import EndpointInterface from "#{API_BASE_URL}/api/rest/endpoint/EndpointInterface.js";

// the request schema, this should be updated
// whenever changes to the endpoint parameters are made
// this is important because this is used to validate and parse the request parameters
const requestSchema = {
  "title" : "announceRequest",
  "id" : "announceRequest",
  "default" : "Schema definition for announce",
  "$schema" : "http://json-schema.org/draft-07/schema",
  "type" : "object",
  "properties" : {
    "compact" : {
      "title" : "compact",
      "id" : "announce_compact",
      "type" : "string",
      "minLength" : 1
    },
    "latitude" : {
      "title" : "latitude",
      "id" : "announce_latitude",
      "default" : "0",
      "type" : "string",
      "minLength" : 1
    },
    "liveness" : {
      "title" : "liveness",
      "id" : "announce_liveness",
      "default" : "0",
      "type" : "string",
      "minLength" : 1
    },
    "sign" : {
      "title" : "sign",
      "id" : "announce_sign",
      "default" : "0",
      "type" : "string",
      "minLength" : 1
    },
    "downloaded" : {
      "title" : "downloaded",
      "id" : "announce_downloaded",
      "default" : "0",
      "type" : "string",
      "minLength" : 1
    },
    "url" : {
      "title" : "url",
      "id" : "announce_url",
      "type" : "string",
      "minLength" : 1
    },
    "peer_id" : {
      "title" : "peer_id",
      "id" : "announce_peer_id",
      "default" : "0",
      "type" : "string",
      "minLength" : 1
    },
    "wallet_id" : {
      "title" : "wallet_id",
      "id" : "announce_wallet_id",
      "default" : "0",
      "type" : "string",
      "minLength" : 1
    },
    "port" : {
      "title" : "port",
      "id" : "announce_port",
      "default" : "0",
      "type" : "string",
      "minLength" : 1
    },
    "left" : {
      "title" : "left",
      "id" : "announce_left",
      "default" : "0",
      "type" : "string",
      "minLength" : 1
    },
    "uploaded" : {
      "title" : "uploaded",
      "id" : "announce_uploaded",
      "default" : "0",
      "type" : "string",
      "minLength" : 1
    },
    "info_hash" : {
      "title" : "info_hash",
      "id" : "announce_info_hash",
      "default" : "0",
      "type" : "string",
      "minLength" : 1
    },
    "event" : {
      "title" : "event",
      "id" : "announce_event",
      "default" : "empty",
      "type" : "string",
      "minLength" : 1
    },
    "longitude" : {
      "title" : "longitude",
      "id" : "announce_longitude",
      "default" : "0",
      "type" : "string",
      "minLength" : 1
    }
  }
}

// the response schema, this should be updated
// whenever changes to the endpoint parameters are made
// this is important because this could be used to parse the result
const responseSchema = {
  "title" : "announceResponse",
  "id" : "announceResponse",
  "default" : "Schema definition for announce",
  "$schema" : "http://json-schema.org/draft-07/schema",
  "type" : "object",
  "properties" : {
    "result" : {
      "title" : "result",
      "type" : "string",
      "minLength" : 1
    }
  }
}

// should contain offline mock data, make sure it adheres to the response schema
const mockResult = {};

class announce extends EndpointInterface {
	constructor() {
		// name and http method, these are inserted when code is generated
		super("announce", "GET");
		this.requestSchema = requestSchema;
		this.responseSchema = responseSchema;
		this.mockResult = mockResult;
	}

	getRequestSchema() {
		return this.requestSchema;
	}

	getResponseSchema() {
		return this.responseSchema;
	}
}

export default new announce();