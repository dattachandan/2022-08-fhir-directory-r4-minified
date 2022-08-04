/*
 * Copyright (c) 2020 HealthLink Limited.
 *
 * This document is copyright. Except for the purpose of fair reviewing, no part
 * of this publication may be reproduced or transmitted in any form or by any
 * means, electronic or mechanical, including photocopying, recording, or any
 * information storage and retrieval system, without permission in writing from
 * the publisher. Infringers of copyright render themselves liable for
 * prosecution.
 * 
 * @author Sajith Jamal
 */

package net.healthlink.fhirdirectory.tasks;

import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;

public class TaskExecMonitor implements Runnable
{
    private ThreadPoolExecutor executor;
    private int seconds;
    private boolean run=true;
    private Logger log;

    public TaskExecMonitor(ThreadPoolExecutor executor, int delay, Logger log)
    {
        this.executor = executor;
        this.seconds=delay;
        this.log=log;
    }
    public void shutdown(){
        this.run=false;
    }
    @Override
    public void run()
    {
        while(run){
        	log.debug(
                    String.format("[monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
                        this.executor.getPoolSize(),
                        this.executor.getCorePoolSize(),
                        this.executor.getActiveCount(),
                        this.executor.getCompletedTaskCount(),
                        this.executor.getTaskCount(),
                        this.executor.isShutdown(),
                        this.executor.isTerminated()));
                try {
                    Thread.sleep(seconds*1000);
                } catch (InterruptedException e) {
                    log.error("{}", e);
                }
        }
            
    }
}
