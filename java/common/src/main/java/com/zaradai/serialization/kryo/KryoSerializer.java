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
import com.google.inject.Inject;
import com.zaradai.serialization.Serializer;

import java.io.InputStream;
import java.io.OutputStream;

public class KryoSerializer implements Serializer {
    private final Kryo kryo;

    @Inject
    public KryoSerializer(Kryo kryo) {
        this.kryo = kryo;
    }

    @Override
    public void serialize(OutputStream out, Object object) {
        Output output = createOutput(out);
        kryo.writeClassAndObject(output, object);
        output.flush();
        output.close();
    }

    protected Output createOutput(OutputStream out) {
        return new Output(out);
    }

    @Override
    public Object deserialize(InputStream in) {
        return kryo.readClassAndObject(createInput(in));
    }

    protected Input createInput(InputStream in) {
        return new Input(in);
    }

    public void register(Class type) {
        kryo.register(type);
    }
}
