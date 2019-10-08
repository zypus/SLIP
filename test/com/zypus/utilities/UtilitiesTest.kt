package com.zypus.utilities

import golem.matrix.mtj.MTJMatrixFactory
import org.junit.Before

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 29/10/2016
 */
class UtilitiesTest {

	@Before
	fun setup() {
		golem.factory = MTJMatrixFactory()
	}

//	@Test
//	fun testToMatrix() {
//		val l = arrayListOf(1.0,2.0,3.0,4.0,5.0,6.0)
//
//		val m = l.toMatrix(2, 3)
//
//		val sum = (mat[1.0, 2.0 end 3.0, 4.0 end 5.0, 6.0] - m).sum()
//		Assert.assertEquals(0.0, sum, 0.0)
//	}

//	@Test
//	fun testToMatrixRowVector() {
//		val l = arrayListOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
//
//		val m = l.toMatrix(1, 6)
//
//		val sum = (mat[1.0, 2.0, 3.0, 4.0, 5.0, 6.0].T - m).sum()
//		Assert.assertEquals(0.0, sum, 0.0)
//	}

//	@Test
//	fun testToMatrixColumnVector() {
//		val l = arrayListOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
//
//		val m = l.toMatrix(6, 1)
//
//		val sum = (mat[1.0, 2.0, 3.0, 4.0, 5.0, 6.0] - m).sum()
//		Assert.assertEquals(0.0, sum, 0.0)
//	}

}
