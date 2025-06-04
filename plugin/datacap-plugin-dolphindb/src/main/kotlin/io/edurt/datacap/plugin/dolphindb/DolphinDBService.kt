package io.edurt.datacap.plugin.dolphindb

import io.edurt.datacap.spi.PluginService

class DolphinDBService : PluginService
{
    override fun validator(): String?
    {
        return "select version() as version"
    }

    override fun driver(): String
    {
        return "com.dolphindb.jdbc.Driver"
    }

    override fun connectType(): String?
    {
        return "dolphindb"
    }
}
