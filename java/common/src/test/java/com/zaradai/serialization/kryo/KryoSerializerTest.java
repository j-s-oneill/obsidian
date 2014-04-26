/**
 * Copyright 2014 Zaradai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zaradai.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class KryoSerializerTest {
    private static final int TEST_VALUE = 42;
    private Kryo kryo;
    private KryoSerializer uut;

    @Before
    public void setUp() throws Exception {
        kryo = mock(Kryo.class);
        uut = new KryoSerializer(kryo);
    }

    @Test
    public void shouldRegister() throws Exception {
        uut.register(KryoSerializerTest.class);

        verify(kryo).register(KryoSerializerTest.class);
    }

    @Test
    public void shouldDeserialize() throws Exception {
        final Input input = mock(Input.class);
        uut = new KryoSerializer(kryo) {
            @Override
            protected Input createInput(InputStream in) {
                return input;
            }
        };

        uut.deserialize(null);

        verify(kryo).readClassAndObject(input);
    }

    @Test
    public void shouldSerialize() throws Exception {
        TestSerialization test = new TestSerialization(TEST_VALUE);
        final Output output = mock(Output.class);
        uut = new KryoSerializer(kryo) {
            @Override
            protected Output createOutput(OutputStream out) {
                return output;
            }
        };

        uut.serialize(null, test);

        verify(kryo).writeClassAndObject(output, test);
    }

    @Test
    public void shouldDeserlizeSerializedObject() throws Exception {
        Kryo serializer = new Kryo();
        TestSerialization test = new TestSerialization(TEST_VALUE);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(256);
        KryoSerializer uut = new KryoSerializer(serializer);

        uut.serialize(outputStream, test);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        Object res = uut.deserialize(inputStream);

        assertThat(res, not(nullValue()));
        assertThat(res, instanceOf(TestSerialization.class));
        assertThat(((TestSerialization) res).getTest(), is(TEST_VALUE));
    }

    @Test
    public void shouldDeserlizeSerializedRegisteredObject() throws Exception {
        Kryo serializer = new Kryo();
        TestSerialization test = new TestSerialization(TEST_VALUE);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(256);
        KryoSerializer uut = new KryoSerializer(serializer);

        uut.register(TestSerialization.class);

        uut.serialize(outputStream, test);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        Object res = uut.deserialize(inputStream);

        assertThat(res, not(nullValue()));
        assertThat(res, instanceOf(TestSerialization.class));
        assertThat(((TestSerialization) res).getTest(), is(TEST_VALUE));
    }
}
