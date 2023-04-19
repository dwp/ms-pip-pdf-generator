#!/bin/bash

KEY_ID=$(awslocal kms create-key --query 'KeyMetadata.KeyId' --output text)
awslocal kms create-alias --alias-name alias/localstack --target-key-id "$KEY_ID"
awslocal kms describe-key --key-id alias/localstack
