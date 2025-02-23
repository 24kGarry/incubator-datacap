package io.edurt.datacap.notify.dingtalk

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.TypeLiteral
import io.edurt.datacap.notify.NotifyManager
import io.edurt.datacap.notify.NotifyService
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class DingTalkModuleTest
{
    private val name = "DingTalk"
    private var injector: Injector? = null

    @Before
    fun before()
    {
        injector = Guice.createInjector(NotifyManager())
    }

    @Test
    fun test()
    {
        val notifyService: NotifyService? = injector?.getInstance(Key.get(object : TypeLiteral<Set<NotifyService>>()
        {}))
            ?.first { it.name() == name }
        assertNotNull(notifyService)
    }
}
