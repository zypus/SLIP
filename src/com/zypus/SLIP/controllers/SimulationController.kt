package com.zypus.SLIP.controllers

import com.zypus.math.LineSegment
import com.zypus.math.Vector2
import com.zypus.math.intersect
import com.zypus.math.lerp
import com.zypus.SLIP.models.SimulationSetting
import com.zypus.SLIP.models.SimulationState
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
                val (dt) = setting
                var (pos, v, a, L, l, k, M, R, sPos, v0, p0, c) = state.slip
                val (x, y) = pos
                val (G,terrain) = state.environment
            // Compute the height from the tip off the spring to the mass center.
                val h = cos(a) * (L + R);
            // If the height h is less than the distance to the ground y, the system is in the stance phase.
                val ty = terrain(x - sin(a) * (L + R))
                if (y < h+ ty) {
                    // Compute the angle a between the stance point and the center of the mass.
                        a = atan2(x - sPos.x, y-sPos.y)
                    // Compute the current length l of the spring.
                        l = ((y- sPos.y) / cos(a)) - R
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
                        a = atan2(pos.x - sPos.x, pos.y- sPos.y)
                    // Compute the current length l of the spring.
                        l = ((pos.y- sPos.y) / cos(a)) - R
                }
            // Else the system is in the flight phase.
                else {
                        // Get the controller input
                        a =  a.lerp(c.control(state.slip), 0.05)
                    // Set the current length l of the spring back to the rest length L.
                        l = L;
                    // Compute the time until the system touches the grcound again.
//                        val (t1, t2) = midnight(G.y, v0.y, p0.y)
                    // Compute the landing coordinate of the spring tip sx.
//                        sPos = Vector2((v0.x * t2!! + p0.x) - sin(a) * (L + R), 0.0);
                    // Apply gravity.
                        v += G * dt
                    // Update the position.
                        pos += v * dt
                        // Check if the spring touches the ground.
                        val pTip = Vector2(x,y) - Vector2(sin(a) * (L + R), cos(a) * (L + R))
                        val tip = pos - Vector2(sin(a) * (L + R), cos(a) * (L + R))
                        if (pos.y < h + terrain(tip.x)) {
                                sPos = (LineSegment(pTip, tip) intersect LineSegment(Vector2(pTip.x, terrain(pTip.x)), Vector2(tip.x, terrain(tip.x)))) ?: Vector2(tip.x, terrain(tip.x))
                                a = atan2(pos.x - sPos.x, pos.y - terrain(pos.x - sin(a) * (L + R)))
                                l = ((pos.y - terrain(pos.x - sin(a) * (L + R))) / cos(a)) - R
                        }
                }
            // If the mass touches the ground stop the movement.
                if (pos.y < R+terrain(pos.x)) {
                    pos = Vector2(pos.x - dt * v.x, R+terrain(pos.x))
                    v = Vector2(0, 0)
                }

                return state.copy(slip = state.slip.copy(position = pos, velocity = v, angle = a, length = l, standPosition = sPos, flightVelocity = v0, headPosition = p0))
        }

    }

}