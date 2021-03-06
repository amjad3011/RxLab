package com.github.anastr.rxlab.activities

import android.os.Bundle
import android.view.View
import com.github.anastr.rxlab.objects.drawing.ObserverObject
import com.github.anastr.rxlab.objects.drawing.TextOperation
import com.github.anastr.rxlab.objects.emits.BallEmit
import com.github.anastr.rxlab.objects.emits.EmitObject
import com.github.anastr.rxlab.objects.time.TimeObject
import com.github.anastr.rxlab.view.Action
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.activity_operation.*
import java.util.concurrent.TimeUnit

/**
 * Created by Anas Altair on 4/10/2020.
 */
class ThrottleWithTimeoutActivity: OperationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCode("Observable.<String>create(emitter -> {\n" +
                "    fab.setOnClickListener(v -> {\n" +
                "        if (!emitter.isDisposed())\n" +
                "            emitter.onNext(\"emit\");\n" +
                "    });\n" +
                "})\n" +
                "        // also called debounce\n" +
                "        .throttleWithTimeout(2, TimeUnit.SECONDS)\n" +
                "        .subscribe();")

        addNote("add 1 emit and wait, then try to add emits rapidly.")

        fab.visibility = View.VISIBLE

        val throttleObject = TextOperation("throttleWithTimeout", "2 sec")
        surfaceView.addDrawingObject(throttleObject)
        val observerObject = ObserverObject("Observer")
        surfaceView.addDrawingObject(observerObject)

        var timeObject: TimeObject? = null
        Observable.create<EmitObject> { emitter ->
            fab.setOnClickListener {
                if (!emitter.isDisposed)
                    emitter.onNext(BallEmit("emit"))
            }
        }
            .doOnNext {
                surfaceView.action(Action(0) { doOnRenderThread {
                    timeObject?.let { time ->
                        if (!time.locked)
                            observerObject.removeTime(time)
                    }
                    observerObject.startTime(TimeObject.Lock.AFTER).apply { timeObject = this }
                } })
            }
            .throttleWithTimeout(2, TimeUnit.SECONDS)
            .subscribe({
                val thread = Thread.currentThread().name
                surfaceView.action(Action(0) {
                    it.checkThread(thread)
                    timeObject?.lock()
                    addEmit(throttleObject, it)
                    moveEmit(it, throttleObject, observerObject)
                })
            }, errorHandler, {
                surfaceView.action(Action(0) { doOnRenderThread { observerObject.complete() } })
            })
            .disposeOnDestroy()
    }
}
