package com.cloud9ers.lxc

import scala.sys.process._

class ContainerNotExists(message: String) extends Exception(message)
class ContainerAlreadyExists(message: String) extends Exception(message)
class ContainerCreationException(message: String) extends Exception(message)

object LXC {
	private def executeOn[T](name: String, onExisting: Boolean=true)(c: => T): T = {
		if (exists(name) != onExisting)
			if (onExisting == false) 
				throw new ContainerAlreadyExists("The container(" + name + ") does not exist!")
			else
				throw new ContainerNotExists("The container(" + name + ") does not exist!")
		else	
			c
	}

	def create(name: String, configFile: String=null, 
		template: String=null, 
		backingStore: String=null, 
		templateOptions: Seq[String]=Nil): Unit = {

		executeOn(name, false) {
			var cmd = Seq("lxc-create", "-n", name)
			if (configFile != null) cmd ++= Seq("-f", configFile)
			if (template != null) cmd ++= Seq("-t", template)
			if (backingStore != null) cmd ++= Seq("-B", backingStore)
			if (templateOptions.nonEmpty) cmd ++= Seq("--") ++ templateOptions
			if (cmd.! == 0) {
					if (! exists(name)) //verify
						throw new ContainerNotExists("Container " + name + " doesn't seem to be created!")
					//created!
				} else {
					throw new ContainerCreationException("Creation error of container " + name)
				}

		}
	}

	def exists(name: String): Boolean = {
		if (allAsList contains name) true else false
	}

	def allAsList(): Seq[String] = {
		allAsMap.values.flatten.toList
	}

	def allAsMap(): Map[String, Seq[String]] = {
		val lines = "lxc-list".!!.split('\n').toList
		val running = lines.slice(lines.indexWhere(_ == "RUNNING")+1, lines.indexWhere(_ == "STOPPED"))
							.filter(_.nonEmpty)
							.map(_.trim)

		val stopped = lines.slice(lines.indexWhere(_ == "STOPPED")+1, lines.length)
							.filter(_.nonEmpty)
							.map(_.trim)

		Map("running" -> running, "stopped" -> stopped)
	}

	def start(name: String, configFile: String=null): Int = {
		executeOn(name, true) {
			var cmd = Seq("lxc-start", "-n", name, "-d")
			if (configFile != null) cmd ++= Seq("-f", configFile)
			cmd.!
		}
	}

	def stop(name: String): Int = {
		executeOn(name, true) {
			Seq("lxc-stop", "-n", name).!
		}
	}

	def kill(name: String, signal: String): Int = {
		executeOn(name, true) {
			Seq("lxc-kill", "-n", name, signal).!		
		}
	}

	def shutdown(name: String, wait: Boolean=false, reboot: Boolean=false): Int = {
		executeOn(name, true) {
			var cmd = Seq("lxc-shutdown", "-n", name)
			if (wait) cmd ++= Seq("-w")
			if (reboot) cmd ++= Seq("-r")
			cmd.!
		}
	}

	def destroy(name: String): Int = {
		executeOn(name, true) {
			Seq("lxc-destroy", "-f", "-n", name).!
		}
	}

	def freeze(name: String): Int = {
		executeOn(name, true) {
			Seq("lxc-freeze", "-n", name).!
		}
	}

	def unfreeze(name: String): Int = {
		Seq("lxc-unfreeze", "-n", name).!
	}

	def info(name: String): Map[String, String] = {
			val cmd = Seq("lxc-info", "--name", name)
			Map(cmd.!!.split('\n').map(s => {val x = s.split(':'); (x(0).trim, x(1).trim)}): _*)		
	}

	def checkconfig(): String = {
		"lxc-checkconfig".!!
	}

	lazy val stopped: Seq[String] =
		allAsMap()("stopped")

	lazy val running: Seq[String] =
		allAsMap()("running")


}