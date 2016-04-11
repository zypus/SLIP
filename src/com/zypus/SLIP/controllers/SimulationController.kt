package com.zypus.SLIP.controllers

import com.zypus.SLIP.models.SimulationSetting
import com.zypus.SLIP.models.SimulationState
import com.zypus.utilities.Vector2
import com.zypus.utilities.lerp
import com.zypus.utilities.rungeKutta
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
			if (y - eps < h + ty) {
				// Compute the angle a between the stance point and the center of the mass.
				a = atan2(pos.x - sPos.x, pos.y - sPos.y)
				// Compute the current length l of the spring.
				l = ((pos.y - sPos.y) / cos(a)) - R
				// Determine the compression X of the spring.
				val X = L - l;
				// Compute the acceleration aS=(aSx,aSy) of the spring.
				val aS = Vector2(sin(a), cos(a)) * k * X / M

				// Update the velocity.
				v += (G + aS) * dt
				// Update the position of the system.
				pos += v * dt

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
					val na = a.lerp(c.control(state.slip) % PI, 0.2)
					if (y > cos(na) * (L + R) + terrain(x - sin(na) * (L + R))) {
						a = na
					}
				}
				h = cos(a) * (L + R);
				// Set the current length l of the spring back to the rest length L.
				l = L;
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

class SimulationController2 {

	companion object {

		fun step(state: SimulationState, setting: SimulationSetting): SimulationState {
			var (dt, eps) = setting
			var (pos, v, a, L, l, k, M, R, sPos, v0, p0, c) = state.slip
			val (G, Fg) = state.environment
			// Compute the height from the tip off the spring to the mass center.
			var h = cos(a) * (L + R);
			// If the height h is less than the distance to the ground y, the system is in the stance phase.
			val ty = Fg(pos.x - sin(a) * (L + R))

			if (pos.y <= h + ty) {

				val sq: (Double) -> Double = { it*it }
				val Fs: (Double,Double) -> Double = { ll, dl -> k*(L-ll) }
				var x: List<Double> = arrayListOf(pos.x, pos.y, v.x, v.y)
				val f: List<(List<Double>) -> Double> = arrayListOf(
						{ x -> x[2] }, // x' = v.x
						{ x -> x[3] }, // y' = v.y
						{ x -> Fs( sqrt( sq(x[0] - sPos.x) + sq(x[1] - Fg(sPos.x)) ) - R, ((x[0]-sPos.x)*x[2] + (x[1] - Fg(sPos.x))*x[3]) / sqrt(sq(x[0] - sPos.x) + sq(x[1] - Fg(sPos.x))) ) * sin(atan2((x[0]-sPos.x), (x[1] - Fg(sPos.x)))) / M }, // v.x' = k * (X*sin(a)) / M
						{ x -> Fs(sqrt(sq(x[0] - sPos.x) + sq(x[1] - Fg(sPos.x))) - R, ((x[0] - sPos.x) * x[2] + (x[1] - Fg(sPos.x)) * x[3]) / sqrt(sq(x[0] - sPos.x) + sq(x[1] - Fg(sPos.x)))) * cos(atan2((x[0] - sPos.x), (x[1] - Fg(sPos.x)))) / M + G.y } // v.y' = k * (X*cos(a)) / M + G.y
				)


				var nx = rungeKutta(x, f, dt)

				// Compute the angle a between the stance point and the center of the mass.
				a = atan2(x[0] - sPos.x, x[1] - sPos.y)

				// Compute the position of the spring tip.
				val tipDisplacement = Vector2(sin(a) * (L + R), cos(a) * (L + R))
				var tip = Vector2(nx[0], nx[1]) - tipDisplacement
//				var tip = Vector2(nx[4], Fg(nx[4]))
				// As long as the spring lifts of the ground recompute with a smaller time step.
				if (nx[1] > h + Fg(tip.x) ) {
					while (abs(nx[1] - (h + Fg(tip.x))) >= eps) {
						if (nx[1] > h + Fg(tip.x) ) {
							dt *= 0.5
						}
						else {
							x = nx
						}
						nx = rungeKutta(x, f, dt)
						a = atan2(x[0] - sPos.x, x[1] - sPos.y)
						tip = Vector2(nx[0], nx[1]) - Vector2(sin(a) * (L + R), cos(a) * (L + R))
					}
				}

				// Update the velocity.
				v = Vector2(nx[2], nx[3])
				// Update the position of the system.
				pos = Vector2(nx[0], nx[1])

				// Compute the angle a between the stance point and the center of the mass.
				a = atan2(pos.x - sPos.x, pos.y - sPos.y)
				// Compute the current length l of the spring.
				l = ((pos.y - sPos.y) / cos(a)) - R
			}
			// Else the system is in the flight phase.
			else {
				// Get the controller input and update angle but only if the spring remains above the ground.
				if (v.y < 0) {
					val control = min(max(-PI,c.control(state.slip)),PI)
					val na = a.lerp(control, 1.0)
					if (pos.y > cos(na) * (L + R) + Fg(pos.x - sin(na) * (L + R))) {
						a = na
					}
				}
				// NOTE Hack the position such that the spare spring energy is preserved
				if (l != L) {
					val spareEnergy = 0.5 * k * Math.pow(L - l, 2.0)
					val p = spareEnergy / (M * -G.y)
					val dp = Vector2(0.0, p)
					pos += dp
					// Set the current length l of the spring back to the rest length L.
					l = L
				}
				var x: List<Double> = arrayListOf(pos.x, pos.y, v.x, v.y)
				val f: List<(List<Double>) -> Double> = arrayListOf(
						{ x -> x[2] }, // x' = v.x
						{ x -> x[3] }, // y' = v.y
						{ x -> 0.0 }, // v.x' = k * (X*sin(a)) / M
						{ x -> G.y }    // v.y' = k * (X*cos(a)) / M + G.y
				)

				var nx = rungeKutta(x, f, dt)
				// NOTE This is the most crucial part of this simulation: To determine where the spring will land. Every inaccuracy will result in energy loss.
				// Compute the position of the spring tip.
				val tipDisplacement = Vector2(sin(a) * (L + R), cos(a) * (L + R))
				var tip = Vector2(nx[0], nx[1]) - tipDisplacement
				// As long as the spring penetrates the ground recompute with a smaller time step.
				if (tip.y < Fg(tip.x) ) {
					while (abs(tip.y - Fg(tip.x)) >= eps) {
						if (tip.y < Fg(tip.x) ) {
							dt *= 0.5
						}
						else {
							x = nx
						}
						nx = rungeKutta(x, f, dt)
						tip = Vector2(nx[0], nx[1]) - tipDisplacement
					}
				}
				// Translate the state vector back to the state representation.
				v = Vector2(nx[2], nx[3])
				pos = Vector2(nx[0], nx[1])
				sPos = pos - tipDisplacement
			}

			// If the mass touches the ground stop the movement.
			if (pos.y < R + Fg(pos.x)) {
				pos = Vector2(pos.x - dt * v.x, R + Fg(pos.x))
				v = Vector2(0, 0)
			}

			return state.copy(slip = state.slip.copy(position = pos, velocity = v, angle = a, length = l, standPosition = sPos, flightVelocity = v0, headPosition = p0))
		}

	}

}