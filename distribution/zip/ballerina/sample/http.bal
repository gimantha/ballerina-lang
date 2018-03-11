import ballerina.net.http;

endpoint<http:ServiceEndpoint> ep1 {
port:9090
}

endpoint<http:ClientEndpoint> ep2 {
serviceUri: "http://www.mocky.io"
}

@http:httpServiceConfig {
basePath:"/hello",
endpoints: [ep1]
}
service<http:HttpService> hello {

@http:resourceConfig {
methods:["GET"],
path:"/"
}
resource sayHello (http:ServerConnector conn, http:InRequest req) {
// http:OutResponse res = {};
// res.setStringPayload("Hello World !!!");
var payload, _ = req.getBinaryPayload();
http:OutRequest req2 = {};
var connectorrr = ep2.getConnector();
var resp, err = connectorrr->get("/v2/5aa3a485310000461026e292", req2);
_ = conn->forward(resp);
}
}
