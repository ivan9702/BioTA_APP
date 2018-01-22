package com.startek.biota.app.network.webservices;

import org.jdeferred.impl.DefaultDeferredManager;

import java.util.concurrent.Executors;

/**
 * Created by skt90u on 2016/6/27.
 */
public class SequenceDeferredManager extends DefaultDeferredManager {

    public SequenceDeferredManager()
    {
        super(Executors.newSingleThreadExecutor());
    }
}
