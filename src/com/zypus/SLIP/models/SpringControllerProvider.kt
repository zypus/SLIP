package com.zypus.SLIP.models

import com.zypus.SLIP.models.SLIP
import com.zypus.SLIP.models.SpringController

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 25/02/16
 */

interface SpringControllerProvider {

	fun createController(): SpringController

}
