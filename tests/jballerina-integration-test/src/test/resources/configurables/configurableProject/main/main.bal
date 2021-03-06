// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/test;
import ballerina/jballerina.java;

configurable int intVar = 5;
configurable byte byteVar = ?;
configurable float floatVar = 9.5;
configurable string stringVar = ?;
configurable boolean booleanVar = ?;
configurable decimal decimalVar = 10.1;

configurable int[] & readonly intArr = ?;
configurable byte[] & readonly byteArr = ?;
configurable float[] & readonly floatArr = [9.0, 5.3, 5.6];
configurable string[] & readonly stringArr = ["apple", "orange", "banana"];
configurable boolean[] & readonly booleanArr = [true, true];
configurable decimal[] & readonly decimalArr = ?;

type AuthInfo record {|
    readonly string username;
    int id = 100;
    string password;
    string[] scopes?;
    boolean isAdmin = false;
|};

type Employee record {|
    readonly int id;
    readonly string name = "Default";
    readonly float salary?;
|};

type Person readonly & record {|
    readonly string name;
    string address = "default address";
    int age?;
|};

type PersonInfo readonly & record {|
    string name;
    string address = "Colombo";
    int age?;
|};

type EmployeeInfo record {|
    int id;
    string name= "test";
    float salary?;
|};

type UserTable table<AuthInfo> key(username);

type EmployeeTable table<Employee> key(id) & readonly;

type PersonTable table<Person> key(name) & readonly;

type nonKeyTable table<AuthInfo>;

type PersonInfoTable table<PersonInfo> & readonly;

type EmpInfoTable table<EmployeeInfo>;

configurable UserTable & readonly users = ?;
configurable EmployeeTable employees = ?;
configurable PersonTable people = ?;
configurable nonKeyTable & readonly nonKeyUsers = ?;
configurable PersonInfoTable peopleInfo = ?;
configurable EmpInfoTable & readonly empInfoTab = ?;

public function main() {
    testSimpleValues();
    testArrayValues();
    testTableValues();

    print("Tests passed");
}

function testSimpleValues() {
    test:assertEquals(42, intVar);
    test:assertEquals(3.5, floatVar);
    test:assertEquals("abc", stringVar);
    test:assertTrue(booleanVar);

    decimal result = 24.87;
    test:assertEquals(result, decimalVar);

    byte result2 = 22;
    test:assertEquals(byteVar, result2);
}

function testArrayValues() {
    test:assertEquals([1, 2, 3], intArr);
    test:assertEquals([9.0, 5.6], floatArr);
    test:assertEquals(["red", "yellow", "green"], stringArr);
    test:assertEquals([true, false, false, true], booleanArr);

    decimal[] & readonly resultArr = [8.9, 4.5, 6.2];
    test:assertEquals(resultArr, decimalArr);

    byte[] & readonly resultArr2 = [11, 22, 33, 44, 55, 66, 77, 88, 99];
    test:assertEquals(byteArr, resultArr2);
}

function testTableValues() {

    test:assertEquals(3, users.length());
    test:assertEquals(3, nonKeyUsers.length());
    test:assertEquals(3, employees.length());
    test:assertEquals(3, people.length());
    test:assertEquals(3, peopleInfo.length());
    test:assertEquals(3, empInfoTab.length());

    AuthInfo & readonly user1 = {
        username: "alice",
        id: 11,
        password: "password1",
        scopes: ["write"]
    };

    AuthInfo & readonly user2 = {
        username: "bob",
        id: 22,
        password: "password2",
        scopes: ["write", "read"]
    };

    AuthInfo & readonly user3 = {
        username: "john",
        id: 33,
        password: "password3"
    };

    test:assertEquals(user1, users.get("alice"));
    test:assertEquals(user2, users.get("bob"));
    test:assertEquals(user3, users.get("john"));

    Employee emp1 = {
        id: 111,
        name: "anna"
    };

    Employee emp2 = {
        id: 222,
        name: "elsa",
        salary: 25000.0
    };

    Employee emp3 = {
        id: 333,
        name: "tom"
    };

    test:assertEquals(emp1, employees.get(111));
    test:assertEquals(emp2, employees.get(222));
    test:assertEquals(emp3, employees.get(333));

    Person person1 = {
        name: "alice",
        address: "London",
        age: 22
    };

    Person person2 = {name: "bob"};

    Person person3 = {
        name: "john",
        age: 25
    };

    test:assertEquals(person1, people.get("alice"));
    test:assertEquals(person2, people.get("bob"));
    test:assertEquals(person3, people.get("john"));

    testTableIterator(users);
    testTableIterator(nonKeyUsers);
    testTableIterator(employees);
    testTableIterator(people);
    testTableIterator(peopleInfo);
    testTableIterator(empInfoTab);
}

function testTableIterator(table<map<anydata>> tab) {
    int count = 0;
    foreach var entry in tab {
        count += 1;
    }
    test:assertEquals(3, count);
}

//Extern methods to verify no errors while testing
function system_out() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

function println(handle receiver, handle arg0) = @java:Method {
    name: "println",
    'class: "java.io.PrintStream",
    paramTypes: ["java.lang.String"]
} external;

function print(string str) {
    println(system_out(), java:fromString(str));
}
