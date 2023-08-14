@xmldata:Name {value: "item"}
type Item record {
    int ItemCode;
};

type OtherItem record {
    string ItemCode;
};

@xmldata:Name {value: "codes"}
type Codes record {
    (Item|decimal|int|string)[] item;
    OtherItem OtherItem;
};

@xmldata:Name {value: "bookstore"}
type Bookstore record {
    string storeName;
    int postalCode;
    boolean isOpen;
    Codes codes;
    @xmldata:Attribute
    string status;
    @xmldata:Attribute
    string xmlns\:ns0;
};
