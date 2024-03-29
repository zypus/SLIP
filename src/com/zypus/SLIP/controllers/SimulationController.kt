package com.zypus.SLIP.controllers

import com.zypus.SLIP.models.SimulationSetting
import com.zypus.SLIP.models.SimulationState
import com.zypus.utilities.lerp
import com.zypus.utilities.rungeKutta4
import com.zypus.utilities.squared
import mikera.vectorz.Vector2
import mikera.vectorz.Vector4
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
			var (pos, v, a, L, l, k, M, R, sPos, c, crashed, grounded) = state.slip

			/* If the slip is crashed return immediately without further calculation.*/
			if (crashed) {
				return state
			}

			val (G, terrain) = state.environment
			// Compute the height from the tip off the spring to the mass center.
			var h = cos(a) * (L + R);
			// If the height h is less than the distance to the ground y, the system is in the stance phase.
			val ty = terrain(pos.x - sin(a) * (L + R))

			if (pos.y <= h + ty) {
				grounded = true
				/* System is in the stance phase. */

				/* Check the controller for the new spring value. */
				k = c.constant(state.slip)
				val sq: (Double) -> Double = { it*it }
				val Fs: (Double,Double) -> Double = { ll, dl -> k*(L-ll) }
//				var x: List<Double> = arrayListOf(pos.x, pos.y, v.x, v.y)
//				val f: List<(List<Double>) -> Double> = arrayListOf(
//						{ x -> x[2] }, // x' = v.x
//						{ x -> x[3] }, // y' = v.y
//						{ x -> Fs( sqrt( sq(x[0] - sPos.x) + sq(x[1] - terrain(sPos.x)) ) - R, ((x[0]-sPos.x)*x[2] + (x[1] - terrain(sPos.x))*x[3]) / sqrt(sq(x[0] - sPos.x) + sq(x[1] - terrain(sPos.x))) ) * sin(atan2((x[0]-sPos.x), (x[1] - terrain(sPos.x)))) / M }, // v.x' = k * (X*sin(a)) / M
//						{ x -> Fs(sqrt(sq(x[0] - sPos.x) + sq(x[1] - terrain(sPos.x))) - R, ((x[0] - sPos.x) * x[2] + (x[1] - terrain(sPos.x)) * x[3]) / sqrt(sq(x[0] - sPos.x) + sq(x[1] - terrain(sPos.x)))) * cos(atan2((x[0] - sPos.x), (x[1] - terrain(sPos.x)))) / M + G.y } // v.y' = k * (X*cos(a)) / M + G.y
//				)

				val x = Vector4(pos.x, pos.y, v.x, v.y)
				val sPosX = sPos.x
				val terrain = terrain(sPosX)
				val f: List<(Vector4) -> Double> = arrayListOf(
						{ x -> x[2] }, // x' = v.x
						{ x -> x[3] }, // y' = v.y
						{ x -> val x0 = x[0]
							val x1 = x[1]
							Fs(sqrt(sq(x0 - sPosX) + sq(x1 - terrain)) - R, ((x0 - sPosX) * x[2] + (x1 - terrain) * x[3]) / sqrt(sq(x0 - sPosX) + sq(x1 - terrain))) * sin(atan2((x0 - sPosX), (x1 - terrain))) / M }, // v.x' = k * (X*sin(a)) / M
						{ x -> val x0 = x[0]
							val x1 = x[1]
							Fs(sqrt(sq(x0 - sPosX) + sq(x1 - terrain)) - R, ((x0 - sPosX) * x[2] + (x1 - terrain) * x[3]) / sqrt(sq(x0 - sPosX) + sq(x1 - terrain))) * cos(atan2((x0 - sPosX), (x1 - terrain))) / M + G.y } // v.y' = k * (X*cos(a)) / M + G.y
				)


				var nx = rungeKutta4(x, f, dt)

				/* Compute the angle a between the stance point and the center of the mass. */
//				a = atan2(x[0] - sPos.x, x[1] - sPos.y)

				/* Compute the position of the spring tip. */
//				val tipDisplacement = Vector2(sin(a) * (L + R), cos(a) * (L + R))
//				var tip = Vector2(nx[0], nx[1]) - tipDisplacement
//				var tip = Vector2(nx[4], terrain(nx[4]))

				val LR2 = (L+R).squared
				val eps2 = sqrt(eps)

				/* As long as the spring lifts of the ground recompute with a smaller time step. */
				if ((x[0]- sPosX).squared + (x[1]-sPos.y).squared > LR2 ) {
					var count = 0
					while (abs((x[0] - sPosX).squared + (x[1] - sPos.y).squared - LR2) >= eps2) {
						count++
						if (count > 50) {
							crashed = true
							break
						}
						if ( (x[0] - sPosX).squared + (x[1] - sPos.y).squared > LR2 ) {
							dt *= 0.5
						}
						else {
							x.set(nx)
							dt *= 0.5
						}
						/* Apply the forward approximation again with a smaller step size. */
						nx = rungeKutta4(x, f, dt)

						/* Update the angle, the position of the tip and the height. */
//						a = atan2(x[0] - sPos.x, x[1] - sPos.y)
//						tip = Vector2(nx[0], nx[1]) - Vector2(sin(a) * (L + R), cos(a) * (L + R))
					}
				}

				/* Update the velocity. */
				v.x = nx[2]
				v.y = nx[3]
//				v = Vector2(nx[2], nx[3])

				/* Update the position of the system. */
				pos.x = nx[0]
				pos.y = nx[1]
//				pos = Vector2(nx[0], nx[1])

				/* Compute the angle a between the stance point and the center of the mass. */
				a = atan2(pos.x - sPosX, pos.y - sPos.y)

				/* Compute the current length l of the spring. */
				l = ((pos.y - sPos.y) / cos(a)) - R
			}
			/* Else the system is in the flight phase. */
			else {
				grounded = false
				/* Get the controller input and update angle but only if the spring remains above the ground. */
				if (v.y < 0) {
					val control = min(max(-PI,c.angle(state.slip)),PI)
					val na = a.lerp(control, 0.3)
					if (pos.y > cos(na) * (L + R) + terrain(pos.x - sin(na) * (L + R))) {
						a = na
					}
				}
				/* NOTE Hack the position such that the spare spring energy is preserved */
				if (l != L) {
					val spareEnergy = 0.5 * k * Math.pow(L - l, 2.0)
					val p = spareEnergy / (M * -G.y)
//					val dp = Vector2(0.0, p)
					pos.add(0.0, p)
//					pos += dp
					/* Set the current length l of the spring back to the rest length L. */
					l = L
				}
//				var x: List<Double> = arrayListOf(pos.x, pos.y, v.x, v.y)
//				val f: List<(List<Double>) -> Double> = arrayListOf(
//						{ x -> x[2] },  /* x' = v.x */
//						{ x -> x[3] },  /* y' = v.y */
//						{ x -> 0.0 },   /* v.x' = k * (X*sin(a)) / M */
//						{ x -> G.y }    /* v.y' = k * (X*cos(a)) / M + G.y */
//				)

				val x = Vector4(pos.x, pos.y, v.x, v.y)
				val f : List<(Vector4) -> Double> = arrayListOf(
						{ x -> x[2]},
						{ x -> x[3]},
						{ x -> 0.0},
						{ x -> G.y}
				)

				var nx = rungeKutta4(x, f, dt)
				// NOTE This is the most crucial part of this simulation: To determine where the spring will land. Every inaccuracy will result in energy loss.
				// Compute the position of the spring tip.
				val tipDisplacement = Vector2(sin(a) * (L + R), cos(a) * (L + R))
				val tip = Vector2(nx[0], nx[1])
				tip.sub(tipDisplacement)
				// As long as the spring penetrates the ground recompute with a smaller time step.
				if (tip.y < terrain(tip.x) ) {
					var count = 0
					while (abs(tip.y - terrain(tip.x)) >= eps) {
						count++
						if (count > 100) {
							break
						}
						if (tip.y < terrain(tip.x) ) {
							dt *= 0.5
						}
						else {
							x.set(nx)
							dt *= 0.5
						}
						nx = rungeKutta4(x, f, dt)
						tip.setValues(nx[0], nx[1])
						tip.sub(tipDisplacement)
					}
				}
				// Translate the state vector back to the state representation.
				v.setValues(nx[2], nx[3])
				pos.setValues(nx[0], nx[1])
				sPos.set(pos.subCopy(tipDisplacement))
			}

			// If the mass touches the ground stop the movement.
			if (pos.y < R + terrain(pos.x)) {
				crashed = true
				pos.setValues(pos.x - dt * v.x, R + terrain(pos.x))
				v.setValues(0.0,0.0)
			}

			return state.copy(slip = state.slip.copy(position = pos, velocity = v, angle = a, length = l, springConstant = k, standPosition = sPos, crashed = crashed, grounded = grounded))
		}

	}

}
