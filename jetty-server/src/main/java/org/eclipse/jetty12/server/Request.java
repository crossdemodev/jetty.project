//
// ========================================================================
// Copyright (c) 1995-2021 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package org.eclipse.jetty12.server;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.util.Attributes;
import org.eclipse.jetty.util.Callback;

public interface Request extends Attributes, Callback
{
    String getId();

    Channel getChannel();

    MetaConnection getMetaConnection();

    String getMethod();

    HttpURI getURI();

    HttpFields getHeaders();

    long getContentLength();

    Content readContent();

    void demandContent(Runnable onContentAvailable);

    void onTrailers(Consumer<HttpFields> onTrailers);

    void whenComplete(BiConsumer<Request, Throwable> onComplete);

    default Request getWrapped()
    {
        return null;
    }

    default <R> R as(Class<R> type)
    {
        return (type.isInstance(this) ? (R)this : null);
    }

    class Wrapper extends Attributes.Wrapper implements Request
    {
        private final Request _wrapped;

        public Wrapper(Request wrapped)
        {
            super(wrapped);
            this._wrapped = wrapped;
        }

        @Override
        public String getId()
        {
            return _wrapped.getId();
        }

        @Override
        public MetaConnection getMetaConnection()
        {
            return _wrapped.getMetaConnection();
        }

        @Override
        public Channel getChannel()
        {
            return _wrapped.getChannel();
        }

        @Override
        public String getMethod()
        {
            return _wrapped.getMethod();
        }

        @Override
        public HttpURI getURI()
        {
            return _wrapped.getURI();
        }

        @Override
        public HttpFields getHeaders()
        {
            return _wrapped.getHeaders();
        }

        @Override
        public long getContentLength()
        {
            return _wrapped.getContentLength();
        }

        @Override
        public Content readContent()
        {
            return _wrapped.readContent();
        }

        @Override
        public void demandContent(Runnable onContentAvailable)
        {
            _wrapped.demandContent(onContentAvailable);
        }

        @Override
        public void onTrailers(Consumer<HttpFields> onTrailers)
        {
            _wrapped.onTrailers(onTrailers);
        }

        @Override
        public void whenComplete(BiConsumer<Request, Throwable> onComplete)
        {
            _wrapped.whenComplete(onComplete);
        }

        @Override
        public Request getWrapped()
        {
            return _wrapped;
        }

        @Override
        public void succeeded()
        {
            _wrapped.succeeded();
        }

        @Override
        public void failed(Throwable x)
        {
            _wrapped.failed(x);
        }

        @Override
        public InvocationType getInvocationType()
        {
            return _wrapped.getInvocationType();
        }
    }
}