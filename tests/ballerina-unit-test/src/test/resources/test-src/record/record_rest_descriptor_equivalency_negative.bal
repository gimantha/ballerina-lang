type StringRest record {
    string name;
    int id;
    string...
};

type IntRest record {
    string name;
    int id;
    int...
};

type FloatRest record {
    string name;
    int id;
    float...
};

type BooleanRest record {
    string name;
    int id;
    float...
};

function testEquivOfStringAndIntRestDescriptors() {
    StringRest a = {};
    IntRest b = a;

    b = {};
    StringRest aa = b;
}

function testEquivOfFloatAndIntRestDescriptors() {
    FloatRest a = {};
    IntRest b = a;

    b = {};
    FloatRest aa = b;
}

function testEquivOfBooleanAndIntRestDescriptors() {
    BooleanRest a = {};
    IntRest b = a;

    b = {};
    BooleanRest aa = b;
}

function testEquivOfFloatAndStringRestDescriptors() {
    FloatRest a = {};
    StringRest b = a;

    b = {};
    FloatRest aa = b;
}

function testEquivOfBooleanAndStringRestDescriptors() {
    BooleanRest a = {};
    StringRest b = a;

    b = {};
    BooleanRest aa = b;
}

function testEquivOfBooleanAndFloatRestDescriptors() {
    BooleanRest a = {};
    FloatRest b = a;

    b = {};
    BooleanRest aa = b;
}

type StringRest2 record {
    string name;
    int id;
    float scale;
    string...
};

type RecordRest1 record {
    string id;
    StringRest...
};

type RecordRest2 record {
    string id;
    StringRest2...
};

function testEquivWithRecordsAsRestDescriptors() {
     RecordRest2 a = {};
     RecordRest1 b = a;

     b = {};
     RecordRest2 aa = b;
}
