/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2016 Incapture Technologies LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package reflex.node;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import reflex.IReflexHandler;
import reflex.Scope;
import reflex.ThreadSafeScope;
import reflex.debug.IReflexDebugger;
import reflex.node.parallel.BlockEvaluator;
import reflex.node.parallel.CountingThreadPoolExecutor;
import reflex.value.ReflexValue;
import reflex.value.internal.ReflexVoidValue;

// Parallel for statement

public class PForStatementNode extends BaseNode {

    private String identifier;
    private ReflexNode startExpr;
    private ReflexNode stopExpr;
    private ReflexNode block;

    public PForStatementNode(int lineNumber, IReflexHandler handler, Scope s, String id, ReflexNode start, ReflexNode stop, ReflexNode bl) {
        super(lineNumber, handler, s);
        identifier = id;
        startExpr = start;
        stopExpr = stop;
        block = bl;
    }

    @Override
    public ReflexValue evaluate(IReflexDebugger debugger, Scope scope) {
        debugger.stepStart(this, scope);
        int start = startExpr.evaluate(debugger, scope).asDouble().intValue();
        int stop = stopExpr.evaluate(debugger, scope).asDouble().intValue();
        CountingThreadPoolExecutor parallel = new CountingThreadPoolExecutor(10, 10, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));
        int submitted = 0;
        for (int i = start; i <= stop; i++) {
            // Need to clone Block so that it can be run in parallel, or make
            // the
            // scope thread local ?
            ThreadSafeScope newScope = new ThreadSafeScope(scope);
            newScope.assign(identifier, new ReflexValue(i));
            BlockEvaluator be = new BlockEvaluator(block, newScope);
            submitted++;
            parallel.execute(be);
        }
        // Now wait for it to finish
        parallel.waitForExecCount(submitted);
        parallel.shutdown();
        debugger.stepEnd(this, new ReflexVoidValue(lineNumber), scope);
        return new ReflexVoidValue();
    }
}
