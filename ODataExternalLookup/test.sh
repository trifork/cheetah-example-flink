#!/bin/bash

client_secret=${1:-''}
# Set to true to create an event, false to consume an event
CREATE=${2:-''}

if [ -z "$client_secret" ]; then
    echo "Client secret is required"
    exit 1
fi
if [ -z "$CREATE" ]; then
    echo "CREATE (true/false) is required"
    exit 1
fi

#https://community.sap.com/t5/technology-blogs-by-members/guide-step-by-step-procedure-to-send-and-receive-messages-in-a-sap-event/ba-p/13539647

client_id="sb-default-5cb2acd3-bb26-4281-b23b-084e4e7830b0-clone!b8513|xbem-service-broker-!b2436"
token_url="https://invokerscf.authentication.eu10.hana.ondemand.com/oauth/token"

event_mesh_endpoint=https://enterprise-messaging-pubsub.cfapps.eu10.hana.ondemand.com/messagingrest/v1/queues/trifork%2Fdemo%2Fs4%2Fcheetah/messages

# Function to get OAuth token
function get_oauth_token() {
    # Requesting OAuth token
    response=$(curl -s --request POST --url $token_url \
    --header 'Content-Type: application/x-www-form-urlencoded' \
    --data-urlencode "grant_type=client_credentials" \
    --data-urlencode "client_id=$client_id" \
    --data-urlencode "client_secret=$client_secret")

    # Parsing token from response (assuming response is JSON and contains access_token field)
    token=$(echo $response | jq -r '.access_token')

    if [ -z "$token" ]; then
        echo "Failed to obtain token"
        exit 1
    fi

    echo $token
}

# Main script logic
# Get OAuth token
oauth_token=$(get_oauth_token)

if [ "$CREATE" = "true" ]; then
    echo "Creating event"
    # POST request with OAuth Bearer token authentication
    curl -X POST $event_mesh_endpoint \
        -H "Authorization: Bearer $oauth_token" \
        -H "x-qos: 0" \
        -H "Content-Type: application/json" \
        --data '{"type":"sap.s4.beh.salesorder.v1.SalesOrder.Changed.v1","specversion":"1.0","source":"/default/sap.s4.beh/S4HCLNT100","id":"fffe3055-9701-1ede-b9a6-abc8c8be2bdc","time":"2024-03-18T14:39:47Z","datacontenttype":"application/json","data":{"SalesOrder":"4800"}}'
else
    # POST request with OAuth Bearer token authentication
    curl -X POST $event_mesh_endpoint/consumption \
        -H "Authorization: Bearer $oauth_token" \
        -H "x-qos: 0" \
        -H "Content-Type: application/json"
fi
