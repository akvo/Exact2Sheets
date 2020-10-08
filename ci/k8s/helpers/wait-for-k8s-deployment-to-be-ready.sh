#!/usr/bin/env bash


starttime=$(date +%s)

while [ $(( $(date +%s) - 300 )) -lt "${starttime}" ]; do

   data_portals_status=$(kubectl get pods -l "data-portals-version=$TRAVIS_COMMIT,run=data-portals" -o jsonpath='{range .items[*].status.containerStatuses[*]}{@.name}{" ready="}{@.ready}{"\n"}{end}')
   old_data_portals_status=$(kubectl get pods -l "data-portals-version!=$TRAVIS_COMMIT,run=data-portals" -o jsonpath='{range .items[*].status.containerStatuses[*]}{@.name}{" ready="}{@.ready}{"\n"}{end}')

    if [[ ${data_portals_status} =~ "ready=true" ]] && ! [[ ${data_portals_status} =~ "ready=false" ]] && ! [[ ${old_data_portals_status} =~ "ready" ]] ; then
        echo "all good!"
        exit 0
    else
        echo "Waiting for the containers to be ready"
        sleep 10
    fi
done

echo "Containers not ready after 5 minutes or old containers not stopped"

kubectl get pods -l "run=data-portals" -o jsonpath='{range .items[*].status.containerStatuses[*]}{@.name}{" ready="}{@.ready}{"\n"}{end}'

exit 1