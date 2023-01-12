import EndpointInterface from "#{API_BASE_URL}/api/rest/endpoint/EndpointInterface.js";

// the request schema, this should be updated
// whenever changes to the endpoint parameters are made
// this is important because this is used to validate and parse the request parameters
const requestSchema = {
  "title" : "createAddressRequest",
  "id" : "createAddressRequest",
  "default" : "Schema definition for createAddress",
  "$schema" : "http://json-schema.org/draft-07/schema",
  "type" : "object",
  "properties" : {
    "walletId" : {
      "title" : "walletId",
      "type" : "string",
      "minLength" : 1
    },
    "notes" : {
      "title" : "notes",
      "type" : "string",
      "minLength" : 1
    },
    "city" : {
      "title" : "city",
      "type" : "string",
      "minLength" : 1
    },
    "dialCode" : {
      "title" : "dialCode",
      "type" : "string",
      "minLength" : 1
    },
    "postalCode" : {
      "title" : "postalCode",
      "type" : "string",
      "minLength" : 1
    },
    "latitude" : {
      "title" : "latitude",
      "type" : "string",
      "minLength" : 1
    },
    "isDefault" : {
      "title" : "isDefault",
      "type" : "string",
      "minLength" : 1
    },
    "phoneNumber" : {
      "title" : "phoneNumber",
      "type" : "string",
      "minLength" : 1
    },
    "streetAddress" : {
      "title" : "streetAddress",
      "type" : "string",
      "minLength" : 1
    },
    "countryCode" : {
      "title" : "countryCode",
      "type" : "string",
      "minLength" : 1
    },
    "name" : {
      "title" : "name",
      "type" : "string",
      "minLength" : 1
    },
    "state" : {
      "title" : "State",
      "description" : "State",
      "id" : "state",
      "storages" : [ "SQL" ],
      "type" : "object",
      "properties" : {
        "country" : {
          "title" : "state.country",
          "description" : "Country",
          "id" : "CE_state_country",
          "storages" : [ "SQL" ],
          "nullable" : true,
          "readOnly" : false,
          "versionable" : false,
          "$ref" : "#/definitions/org.meveo.model.billing.Country"
        },
        "stateName" : {
          "title" : "state.stateName",
          "description" : "StateName",
          "id" : "CE_state_stateName",
          "storages" : [ "SQL" ],
          "nullable" : true,
          "readOnly" : false,
          "versionable" : false,
          "type" : "string",
          "maxLength" : 90
        },
        "active" : {
          "title" : "state.active",
          "description" : "Active",
          "id" : "CE_state_active",
          "storages" : [ "SQL" ],
          "default" : "false",
          "nullable" : true,
          "readOnly" : false,
          "versionable" : false,
          "type" : "boolean"
        },
        "stateCode" : {
          "title" : "state.stateCode",
          "description" : "StateCode",
          "id" : "CE_state_stateCode",
          "storages" : [ "SQL" ],
          "nullable" : true,
          "readOnly" : false,
          "versionable" : false,
          "type" : "string",
          "maxLength" : 6
        },
        "id" : {
          "title" : "state.id",
          "description" : "id",
          "id" : "CE_state_id",
          "storages" : [ ],
          "nullable" : false,
          "readOnly" : false,
          "versionable" : false,
          "type" : "integer"
        }
      },
      "required" : [ "id" ]
    },
    "longitude" : {
      "title" : "longitude",
      "type" : "string",
      "minLength" : 1
    }
  }
}

// the response schema, this should be updated
// whenever changes to the endpoint parameters are made
// this is important because this could be used to parse the result
const responseSchema = {
  "title" : "createAddressResponse",
  "id" : "createAddressResponse",
  "default" : "Schema definition for createAddress",
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

class createAddress extends EndpointInterface {
	constructor() {
		// name and http method, these are inserted when code is generated
		super("createAddress", "POST");
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

export default new createAddress();