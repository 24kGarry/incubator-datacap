package io.edurt.datacap.notify.dingtalk

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.TypeLiteral
import io.edurt.datacap.notify.NotifyManager
import io.edurt.datacap.notify.NotifyService
import io.edurt.datacap.notify.model.NotifyRequest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class DingTalkNotifyTest
{
    private val name = "DingTalk"
    private var injector: Injector? = null
    private val request: NotifyRequest = NotifyRequest()

    @Before
    fun before()
    {
        injector = Guice.createInjector(NotifyManager())

        request.access = "ACCESS"
        request.content = "Test Message"
        request.secret = "SECRET"
    }

    @Test
    fun test()
    {
        val notifyService: NotifyService? = injector?.getInstance(Key.get(object : TypeLiteral<Set<NotifyService>>()
        {}))
            ?.first { it.name() == name }
        assertNotNull(notifyService?.send(request))
    }
}
