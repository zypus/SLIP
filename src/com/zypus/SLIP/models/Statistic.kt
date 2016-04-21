package com.zypus.SLIP.models

import java.io.File


/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 04/04/16
 */

class Statistic(vararg titles: String) {

	private val columns: Map<String, MutableList<Any>> = hashMapOf(*(titles.map { it to (arrayListOf<Any>() as MutableList<Any>) }).toTypedArray())
	var rows = 0

	private var _currentRow: Row? = null

	val currentRow: Row?
		get() = _currentRow

	fun newRow(): Row {
		_currentRow = Row((_currentRow?.index ?: -1) + 1, columns)
		rows++
		return _currentRow!!
	}

	class Row(val index: Int, val columns: Map<String, MutableList<Any>>) {

		fun double(key: String) = if (this[key] is Number) (this[key] as Number).toDouble() else null

		fun int(key: String) = if (this[key] is Number) (this[key] as Number).toInt() else null

		fun boolean(key: String) = if (this[key] is Boolean) (this[key] as Boolean) else null

		operator fun get(key: String): Any? {
			return columns[key]?.getOrNull(index)
		}

		operator fun set(key:String, value: Any): Any {
			val list = columns[key]!!
			return if (list.size > index) list.set(index, value) else list.add(value)
		}
	}

	fun toCSV(): String {
		val builder = StringBuilder()
		val keys = columns.keys.filter { !it.startsWith("_") }
		builder.appendln(keys.joinToString())
		(0..rows-1).forEachIndexed { i, v -> builder.appendln(keys.map { k -> columns[k]?.getOrNull(i) }.joinToString {
			if (it is Boolean)  {
				if (it) "1" else "0"
			} else {
				it.toString()
			}
		}) }
		return builder.toString()
	}

	fun writeToFile(fileName: String) {
		val file = File(fileName)
		// Override any existing file.
		file.delete()
		val writer = file.printWriter()
		writer.print(toCSV())
		writer.flush()
		writer.close()
	}

}