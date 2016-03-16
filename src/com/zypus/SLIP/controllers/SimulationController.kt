package com.zypus.SLIP.controllers

import com.zypus.SLIP.models.SimulationSetting
import com.zypus.SLIP.models.SimulationState
import com.zypus.math.Vector2
import com.zypus.math.lerp
import java.lang.Math.*

/**
 * Controller to simulate the SLIP in the given environment.
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 15/02/16
 */

class SimulationController {

	companion object {

		fun step(state: SimulationState, setting: SimulationSetting): SimulationState {
			var (dt, eps) = setting
			var (pos, v, a, L, l, k, M, R, sPos, v0, p0, c) = state.slip
			val (x, y) = pos
			val (G, terrain) = state.environment
			// Compute the height from the tip off the spring to the mass center.
			var h = cos(a) * (L + R);
			// If the height h is less than the distance to the ground y, the system is in the stance phase.
			val ty = terrain(x - sin(a) * (L + R))
			if (y-eps <= h + ty) {
				// Compute the angle a between the stance point and the center of the mass.
				a = atan2(x - sPos.x, y - sPos.y)
				// Compute the current length l of the spring.
				l = ((y - sPos.y) / cos(a)) - R
				// Determine the compression X of the spring.
				val X = L - l;
				// Compute the accelaration aS=(aSx,aSy) of the spring.
				val aS = Vector2(sin(a), cos(a)) * k * X / M
				// Update the velocity.
				v += (aS + G) * dt
				// Save the current velocity as the new velocity for the flight phase v0.
				v0 = v.copy()
				// Update the position of the system.
				pos += v * dt
				// Save the current position as the new position for the flight phase p0;
				p0 = pos.copy()
				// Recompute the angle and the length.
				// Compute the angle a between the stance point and the center of the mass.
				a = atan2(pos.x - sPos.x, pos.y - sPos.y)
				// Compute the current length l of the spring.
				l = ((pos.y - sPos.y) / cos(a)) - R
			}
			// Else the system is in the flight phase.
			else {
				// Get the controller input and update angle but only if the spring remains above the ground.
				if (v.y < 0) {
					val na = a.lerp(c.control(state.slip), 0.2)
					if (y > cos(na) * (L + R) + terrain(x- sin(na) * (L + R))) {
						a = na
					}
				}
				h = cos(a) * (L + R);
				// Set the current length l of the spring back to the rest length L.
				l = L;
				// Compute the time until the system touches the grcound again.
				//                        val (t1, t2) = midnight(G.y, v0.y, p0.y)
				// Compute the landing coordinate of the spring tip sx.
				//                        sPos = Vector2((v0.x * t2!! + p0.x) - sin(a) * (L + R), 0.0);
				// Apply gravity.
				var nv = v + G * dt
				// Update the position.
				var npos = pos + nv * dt
				// NOTE This is the most crucial part of this simulation: To determine where the spring will land. Every inaccuracy will result in energy loss.
				// Check if the spring touches the ground.
				val tipDisplacement = Vector2(sin(a) * (L + R), cos(a) * (L + R))
				var tip = npos - tipDisplacement
				// As long as the spring penetrates the ground recompute with a smaller time step.
				if (npos.y < h + terrain(tip.x) ) {
					while (abs(npos.y - (h + terrain(tip.x))) >= eps) {
						if (npos.y < h + terrain(tip.x) ) {
							dt *= 0.5
						}
						else {
							v = nv
							pos = npos
						}
						nv = v + G * dt
						npos = pos + nv * dt
						tip = npos - tipDisplacement
					}
				}
				v = nv
				pos = npos
				sPos = pos - tipDisplacement
			}
			// If the mass touches the ground stop the movement.
			if (pos.y < R + terrain(pos.x)) {
				pos = Vector2(pos.x - dt * v.x, R + terrain(pos.x))
				v = Vector2(0, 0)
			}

			return state.copy(slip = state.slip.copy(position = pos, velocity = v, angle = a, length = l, standPosition = sPos, flightVelocity = v0, headPosition = p0))
		}

	}

}