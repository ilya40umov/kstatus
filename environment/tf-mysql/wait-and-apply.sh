#!/usr/bin/env sh

terraform init >/dev/null

attempt=1
until terraform plan >/dev/null 2>&1; do
    sleep ${attempt}
    echo "Sleeping after failed 'terraform plan'..."
    attempt=$((attempt + 1))
    if [ ${attempt} = 5 ]
    then
        printf "\033[31mERROR\033[m: Waited too long for 'terraform plan' to succeed!\n"
        exit 1
    fi
done

terraform apply -auto-approve >/dev/null