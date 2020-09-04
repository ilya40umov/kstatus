package me.ilya40umov.kstatus.conf

import com.sksamuel.hoplite.ConfigLoader

// TODO allow overriding the configuration with another yaml file + env variables
inline fun <reified A : Any> loadConfig() =
    ConfigLoader().loadConfigOrThrow<A>("/application.yaml")