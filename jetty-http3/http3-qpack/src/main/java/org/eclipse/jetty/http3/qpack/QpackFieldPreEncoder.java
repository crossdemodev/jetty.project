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

package org.eclipse.jetty.http3.qpack;

import java.nio.ByteBuffer;

import org.eclipse.jetty.http.HttpFieldPreEncoder;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http3.qpack.table.Entry;
import org.eclipse.jetty.util.BufferUtil;

/**
 *  TODO: implement QpackEncoder with PreEncodedFields.
 */
public class QpackFieldPreEncoder implements HttpFieldPreEncoder
{

    @Override
    public HttpVersion getHttpVersion()
    {
        return HttpVersion.HTTP_2;
    }

    @Override
    public byte[] getEncodedField(HttpHeader header, String name, String value)
    {
        boolean notIndexed = QpackEncoder.DO_NOT_INDEX.contains(header);

        ByteBuffer buffer = BufferUtil.allocate(name.length() + value.length() + 10);
        BufferUtil.clearToFill(buffer);
        boolean huffman;
        int bits;

        if (notIndexed)
        {
            // Non indexed field
            boolean neverIndex = QpackEncoder.NEVER_INDEX.contains(header);
            huffman = !QpackEncoder.DO_NOT_HUFFMAN.contains(header);
            buffer.put(neverIndex ? (byte)0x10 : (byte)0x00);
            bits = 4;
        }
        else if (header == HttpHeader.CONTENT_LENGTH && value.length() > 1)
        {
            // Non indexed content length for 2 digits or more
            buffer.put((byte)0x00);
            huffman = true;
            bits = 4;
        }
        else
        {
            // indexed
            buffer.put((byte)0x40);
            huffman = !QpackEncoder.DO_NOT_HUFFMAN.contains(header);
            bits = 6;
        }

        Entry entry = QpackContext.getStaticTable().get(header);
        if (entry != null)
            NBitInteger.encode(buffer, bits, entry.getIndex());
        else
        {
            buffer.put((byte)0x80);
            NBitInteger.encode(buffer, 7, Huffman.octetsNeededLC(name));
            Huffman.encodeLC(buffer, name);
        }

        // TODO: I think we can only encode referencing the static table or with literal representations.
        // QpackEncoder.encodeValue(buffer, huffman, value);

        BufferUtil.flipToFlush(buffer, 0);
        return BufferUtil.toArray(buffer);
    }
}