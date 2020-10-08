#!/usr/bin/env bash


starttime=$(date +%s)

while [ $(( $(date +%s) - 300 )) -lt "${starttime}" ]; do

   akvo_exact_status=$(kubectl get pods -l "akvo-exact-version=$TRAVIS_COMMIT,run=akvo-exact" -o jsonpath='{range .items[*].status.containerStatuses[*]}{@.name}{" ready="}{@.ready}{"\n"}{end}')
   old_akvo_exact_status=$(kubectl get pods -l "akvo-exact-version!=$TRAVIS_COMMIT,run=akvo-exact" -o jsonpath='{range .items[*].status.containerStatuses[*]}{@.name}{" ready="}{@.ready}{"\n"}{end}')

    if [[ ${akvo_exact_status} =~ "ready=true" ]] && ! [[ ${akvo_exact_status} =~ "ready=false" ]] && ! [[ ${old_akvo_exact_status} =~ "ready" ]] ; then
        echo "all good!"
        exit 0
    else
        echo "Waiting for the containers to be ready"
        sleep 10
    fi
done

echo "Containers not ready after 5 minutes or old containers not stopped"

kubectl get pods -l "run=akvo-exact" -o jsonpath='{range .items[*].status.containerStatuses[*]}{@.name}{" ready="}{@.ready}{"\n"}{end}'

exit 1