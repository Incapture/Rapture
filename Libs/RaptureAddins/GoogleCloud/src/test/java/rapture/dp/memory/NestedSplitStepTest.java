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

package rapture.dp.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static rapture.dp.DPTestUtil.ALPHA;
import static rapture.dp.DPTestUtil.makeSignalStep;
import static rapture.dp.DPTestUtil.makeTransition;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import rapture.common.CallingContext;
import rapture.common.QueueSubscriber;
import rapture.common.WorkOrderExecutionState;
import rapture.common.dp.Step;
import rapture.common.dp.WorkOrderStatus;
import rapture.common.dp.Workflow;
import rapture.common.impl.jackson.JacksonUtil;
import rapture.config.ConfigLoader;
import rapture.config.RaptureConfig;
import rapture.dp.WaitingTestHelper;
import rapture.dp.invocable.SignalInvocable;
import rapture.kernel.ContextFactory;
import rapture.kernel.Kernel;
import rapture.kernel.Pipeline2ApiImpl;

public class NestedSplitStepTest {
    private static final String AUTHORITY = "//splitsteptest";
    private CallingContext ctx = ContextFactory.getKernelUser();
    private static final String HELLO = "howdy";
    private static final String SPLIT_STEP = "splitter";
    private static final String SPLIT_STEP_A = "splittera";
    private static final String SPLIT_STEP_B = "splitterb";
    private static final String AFTER_SPLIT = "goodbye";
    private static final String AFTER_SPLIT_B = "goodbyeb";
    private static final String LEFT_A = "left1a";
    private static final String LEFT_CONTINUE_A = "left2a";
    private static final String LEFT_FINISH_A = "left3a";
    private static final String RIGHT_A = "right1a";
    private static final String LEFT_B = "left1b";
    private static final String LEFT_CONTINUE_B = "left2b";
    private static final String LEFT_FINISH_B = "left3b";
    private static final String RIGHT_B = "right1b";
    private static final String WF = "workflow://splitsteptest/workflow";
    private QueueSubscriber subscriber = null;
    private static final int MAX_WAIT = 20000;

    @Before
    public void setup() {
        RaptureConfig config = ConfigLoader.getConf();
        // config.DefaultExchange = "PIPELINE {} USING MEMORY { }";
        config.DefaultExchange = "PIPELINE {} USING MEMORY {  }";
        Kernel.initBootstrap();
        if (!Kernel.getDoc().docRepoExists(ctx, AUTHORITY)) {
            Kernel.getDoc().createDocRepo(ctx, AUTHORITY, "NREP {} USING MEMORY {}");
        }
        // subscriber = Kernel.createAndSubscribe(ALPHA, "PIPELINE {} USING MEMORY { }");
        subscriber = Kernel.createAndSubscribe(ALPHA, "PIPELINE {} USING MEMORY { }");
        createWorkflow();
    }

    @After
    public void tearDown() {
        String[] signals = { HELLO, SPLIT_STEP, SPLIT_STEP_A, SPLIT_STEP_B, AFTER_SPLIT, AFTER_SPLIT_B, LEFT_A, LEFT_CONTINUE_A, LEFT_FINISH_A, RIGHT_A, LEFT_B,
                LEFT_CONTINUE_B, LEFT_FINISH_B, RIGHT_B };
        for (String signal : Arrays.asList(signals)) {
            SignalInvocable.Singleton.clearSignal(signal);
        }
        Kernel.getPipeline2().unsubscribeQueue(ctx, subscriber);
    }

    private void createWorkflow() {
        List<Step> steps = Lists.newArrayList();

        Step step = makeSignalStep(HELLO);
        step.setTransitions(Lists.newArrayList(makeTransition("", SPLIT_STEP)));
        steps.add(step);

        step = new Step();
        step.setName(SPLIT_STEP);
        step.setExecutable("$SPLIT:" + SPLIT_STEP_A + "," + SPLIT_STEP_B);
        step.setTransitions(Lists.newArrayList(makeTransition("", AFTER_SPLIT)));
        steps.add(step);

        step = new Step();
        step.setName(SPLIT_STEP_A);
        step.setExecutable("$SPLIT:" + LEFT_A + "," + RIGHT_A);
        step.setTransitions(Lists.newArrayList(makeTransition("", "$JOIN")));
        steps.add(step);

        step = new Step();
        step.setName(SPLIT_STEP_B);
        step.setExecutable("$SPLIT:" + LEFT_B + "," + RIGHT_B);
        step.setTransitions(Lists.newArrayList(makeTransition("", AFTER_SPLIT_B)));
        steps.add(step);

        step = makeSignalStep(AFTER_SPLIT);
        steps.add(step);

        step = makeSignalStep(AFTER_SPLIT_B);
        step.setTransitions(Lists.newArrayList(makeTransition("", "$JOIN")));
        steps.add(step);

        step = makeSignalStep(RIGHT_A);
        step.setTransitions(Lists.newArrayList(makeTransition("", "$JOIN")));
        steps.add(step);

        step = makeSignalStep(RIGHT_B);
        step.setTransitions(Lists.newArrayList(makeTransition("", "$JOIN")));
        steps.add(step);

        step = makeSignalStep(LEFT_A);
        step.setTransitions(Lists.newArrayList(makeTransition("", LEFT_CONTINUE_A)));
        steps.add(step);

        step = makeSignalStep(LEFT_B);
        step.setTransitions(Lists.newArrayList(makeTransition("", LEFT_CONTINUE_B)));
        steps.add(step);

        step = makeSignalStep(LEFT_CONTINUE_A);
        step.setTransitions(Lists.newArrayList(makeTransition("", LEFT_FINISH_A)));
        steps.add(step);

        step = makeSignalStep(LEFT_CONTINUE_B);
        step.setTransitions(Lists.newArrayList(makeTransition("", LEFT_FINISH_B)));
        steps.add(step);

        step = makeSignalStep(LEFT_FINISH_A);
        step.setTransitions(Lists.newArrayList(makeTransition("", "$JOIN")));
        steps.add(step);

        step = makeSignalStep(LEFT_FINISH_B);
        step.setTransitions(Lists.newArrayList(makeTransition("", "$JOIN")));
        steps.add(step);

        Workflow wf = new Workflow();
        wf.setWorkflowURI(WF);
        wf.setCategory(ALPHA);
        wf.setStartStep(HELLO);
        wf.setSteps(steps);

        Kernel.getDecision().putWorkflow(ctx, wf);
    }

    @Test
    public void runTest() throws InterruptedException {
        // 2 level nested join. A side join joins to the outer, B side join has a step then joins to the outer.
        String workOrderUri = Kernel.getDecision().createWorkOrder(ctx, WF, ImmutableMap.of("testName", "#SimpleSplit"));
        WaitingTestHelper.retry(new Runnable() {
            @Override
            public void run() {
                assertEquals(WorkOrderExecutionState.FINISHED, Kernel.getDecision().getWorkOrderStatus(ctx, workOrderUri).getStatus());
            }
        }, MAX_WAIT * 20);

        assertStatus(workOrderUri, ctx, 25000, WorkOrderExecutionState.FINISHED);
        assertTrue(SignalInvocable.Singleton.testSignal(HELLO));
        assertFalse(SignalInvocable.Singleton.testSignal("Fake Signal"));
        assertTrue(SignalInvocable.Singleton.testSignal(LEFT_A));
        assertTrue(SignalInvocable.Singleton.testSignal(RIGHT_A));
        assertTrue(SignalInvocable.Singleton.testSignal(LEFT_B));
        assertTrue(SignalInvocable.Singleton.testSignal(RIGHT_B));
        assertTrue(SignalInvocable.Singleton.testSignal(LEFT_CONTINUE_A));
        assertTrue(SignalInvocable.Singleton.testSignal(LEFT_CONTINUE_B));
        assertTrue(SignalInvocable.Singleton.testSignal(LEFT_FINISH_B));
        assertTrue(SignalInvocable.Singleton.testSignal(AFTER_SPLIT));
    }

    // helper to assert on the status of a work order. TODO(Oliver): This should be in a utility class.
    private void assertStatus(final String workOrderUri, final CallingContext context, int timeout, final WorkOrderExecutionState expectedStatus)
            throws InterruptedException {
        WaitingTestHelper.retry(new Runnable() {
            @Override
            public void run() {
                WorkOrderStatus status = Kernel.getDecision().getWorkOrderStatus(context, workOrderUri);
                System.out.println(JacksonUtil.formattedJsonFromObject(status));
                assertEquals(expectedStatus, status.getStatus());
            }
        }, timeout);
    }

}
