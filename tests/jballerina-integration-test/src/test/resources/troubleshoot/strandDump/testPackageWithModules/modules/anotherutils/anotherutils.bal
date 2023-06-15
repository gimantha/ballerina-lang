// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/jballerina.java;
import ballerina/lang.runtime;

public function entryfunc() {
    func1();
}

function func1() {
    func2();
}

function func2() {
    func3();
}

function func3() {
    worker w1 {
        sleep_and_wait();
        int x = 10;
        x ->> w2;
        x + 1 -> w2;
    }

    worker w2 {
        println("func3 w2 anotherutils");
        int y = <- w1;
        int z = <- w1;
    }

    wait w1;
}

function sleep_and_wait() {
    sleep_and_wait_nested();
}

function sleep_and_wait_nested() {
    println("sleep_and_wait_nested anotherutils");
    runtime:sleep(100);
}

public function println(string value) {
    handle strValue = java:fromString(value);
    handle stdout1 = stdout();
    printlnInternal(stdout1, strValue);
}

function stdout() returns handle = @java:FieldGet {
    name: "out",
    'class: "java/lang/System"
} external;

function printlnInternal(handle receiver, handle strValue) = @java:Method {
    name: "println",
    'class: "java/io/PrintStream",
    paramTypes: ["java.lang.String"]
} external;
