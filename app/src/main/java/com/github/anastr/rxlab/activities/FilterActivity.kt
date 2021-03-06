package com.github.anastr.rxlab.activities

import android.os.Bundle
import com.github.anastr.rxlab.objects.emits.BallEmit
import com.github.anastr.rxlab.objects.drawing.FixedEmitsOperation
import com.github.anastr.rxlab.objects.drawing.ObserverObject
import com.github.anastr.rxlab.objects.drawing.TextOperation
import com.github.anastr.rxlab.view.Action
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.activity_operation.*

/**
 * Created by Anas Altair on 4/2/2020.
 */
class FilterActivity: OperationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCode("Observable.just(1, 2, 3, 4, 5, 6)\n" +
                "        .filter(emit -> emit % 2 == 0)\n" +
                "        .subscribe();")

        val emits = listOf(
            BallEmit("1"),
            BallEmit("2"),
            BallEmit("3")
            ,
            BallEmit("4"),
            BallEmit("5"),
            BallEmit("6")
        )

        val justOperation = FixedEmitsOperation("just", emits)
        surfaceView.addDrawingObject(justOperation)
        val filterOperation = TextOperation("filter", "emit % 2 == 0")
        surfaceView.addDrawingObject(filterOperation)
        val observerObject = ObserverObject("Observer")
        surfaceView.addDrawingObject(observerObject)

        val actions = ArrayList<Action>()

        Observable.fromIterable(emits)
            .doOnNext { actions.add(Action(1000) { moveEmit(it, justOperation, filterOperation) }) }
            .filter {
                if (it.value.toInt() % 2 == 0)
                    true
                else {
                    actions.add(Action(1000) { dropEmit(it, filterOperation) })
                    false
                }
            }
            .subscribe( {
                actions.add(Action(1000) { moveEmit(it, filterOperation, observerObject) })
            }, errorHandler, {
                actions.add(Action(0) { doOnRenderThread { observerObject.complete() } })
                surfaceView.actions(actions)
            })
            .disposeOnDestroy()
    }
}