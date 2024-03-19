#!/bin/bash

TOKEN_RESPONSE=$(curl -s -X POST "https://api.dropboxapi.com/oauth2/token" \
  --header "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "client_id=$DROPBOX_CLIENT_ID" \
  --data-urlencode "client_secret=$DROPBOX_CLIENT_SECRET" \
  --data-urlencode "refresh_token=$DROPBOX_REFRESH_TOKEN" \
  --data-urlencode "grant_type=refresh_token")

ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.access_token')

curl --fail -X POST https://content.dropboxapi.com/2/files/upload \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --header "Content-Type: application/octet-stream" \
  --header "Dropbox-API-Arg: {\"path\": \"/win86-game.zip\",\"mode\": \"overwrite\",\"mute\": false}" \
  --data-binary "@win86-game.zip"
