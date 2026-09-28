export NODEJS_HOME=/opt/app/nodejs/nodejs_8.9.4
export YARN_HOME=/opt/app/workload/apps/yarn
export PATH=${NODEJS_HOME}/bin:${YARN_HOME}/bin:${PATH}

export HTTP_PROXY=http://sub.proxy.att.com:8080
export HTTPS_PROXY=http://sub.proxy.att.com:8080
export ftp_proxy=http://sub.proxy.att.com:8080
export http_proxy=http://sub.proxy.att.com:8080
export https_proxy=http://sub.proxy.att.com:8080

if [ "$1" = 'epoch-staging2' ]; then
    echo 'Selected source epoch-staging2'
    export SOURCE_CLIENT_ID='5WeaCOykrqu9s3PFVh5msY2k'
    export SOURCE_CLIENT_SECRET='6lXSiFndzSxuHe04s8mHBSe3rZZXRqBc'
    export SOURCE_PROJECT_KEY='epoch-staging2'
    export SOURCE_SCOPES='manage_project:epoch-staging2 manage_api_clients:epoch-staging2 view_api_clients:epoch-staging2'
elif [ "$1" = 'epoch-staging' ]; then
    echo 'Selected source epoch-staging'
    export SOURCE_CLIENT_ID='QvfgGcrqEQbJ_MGzyaJh0Sb_'
    export SOURCE_CLIENT_SECRET='clKJexPspU7UEZtuNXscWQONamI5krCP'
    export SOURCE_PROJECT_KEY='epoch-staging'
    export SOURCE_SCOPES='manage_project:epoch-staging manage_api_clients:epoch-staging view_api_clients:epoch-staging'
elif [ "$1" = 'epoch-dev' ]; then
    echo 'Selected source epoch-dev'
    export SOURCE_CLIENT_ID='VjADKLsGAKoUAXXQTD2L4uvB'
    export SOURCE_CLIENT_SECRET='wpUuMPEW46NR_7E0CFPyTmzZqNCInQMz'
    export SOURCE_PROJECT_KEY='epoch-dev'
    export SOURCE_SCOPES='manage_project:epoch-dev manage_api_clients:epoch-dev view_api_clients:epoch-dev'
elif [ "$1" = 'epoch-production8' ]; then
    echo 'Selected source epoch-production8'
    export SOURCE_CLIENT_ID='UazPpk_l5qq9fPgiNNleEZVJ'
    export SOURCE_CLIENT_SECRET='dxso4nuPu4_oDgqvX9rOOSL4ApTSnN-N'
    export SOURCE_PROJECT_KEY='epoch-production8'
    export SOURCE_SCOPES='manage_project:epoch-production8 manage_api_clients:epoch-production8 view_api_clients:epoch-production8'
elif [ "$1" = 'epoch-production7' ]; then
    echo 'Selected source epoch-production7'
    export SOURCE_CLIENT_ID='7GJbC6SLJM1btsLHo9cPMsoc'
    export SOURCE_CLIENT_SECRET='ADIjH-l6BkB0pmtCJaCrBO6jf0YKD6ZW'
    export SOURCE_PROJECT_KEY='epoch-production7'
    export SOURCE_SCOPES='manage_project:epoch-production7 manage_api_clients:epoch-production7 view_api_clients:epoch-production7'
elif [ "$1" = 'epoch-production10' ]; then
    echo 'Selected source epoch-production10'
    export SOURCE_CLIENT_ID='F8aGRiPuAUGsZN8m7QjFu0Fp'
    export SOURCE_CLIENT_SECRET='e5UC_9ZaDwFjhCJ3FkwyPHycCMDJXYWK'
    export SOURCE_PROJECT_KEY='epoch-production10'
    export SOURCE_SCOPES='manage_project:epoch-production10 manage_api_clients:epoch-production10 view_api_clients:epoch-production10'
elif [ "$1" = 'epoch-production9' ]; then
    echo 'Selected source epoch-production9'
    export SOURCE_CLIENT_ID='9rB6B3bJt5VqdgDZGty0xj3M'
    export SOURCE_CLIENT_SECRET='RbZ1jsh5OCE6k0M8GYMFaJRLywqYx3-G'
    export SOURCE_PROJECT_KEY='epoch-production9'
    export SOURCE_SCOPES='manage_project:epoch-production9 manage_api_clients:epoch-production9 view_api_clients:epoch-production9'
elif [ "$1" = 'epoch-dataload' ]; then
    echo 'Selected source epoch-dataload'
    export SOURCE_CLIENT_ID='6zvGDmcxx_kt6QkRPXzX6axF'
    export SOURCE_CLIENT_SECRET='bqtdmFHPrRNvoP0AH_LiultyEAxNNYKc'
    export SOURCE_PROJECT_KEY='epoch-dataload'
    export SOURCE_SCOPES='manage_project:epoch-dataload manage_api_clients:epoch-dataload view_api_clients:epoch-dataload'
elif [ "$1" = 'mt-epoch-staging' ]; then
    echo 'Selected source mt-epoch-staging'
    export SOURCE_CLIENT_ID='lom_T-CZu3JA2N3osKYl4g74'
    export SOURCE_CLIENT_SECRET='QAn64M-63n4rlpFyt2YQZmNq4TYoyfn3'
    export SOURCE_PROJECT_KEY='epoch-staging'
    export SOURCE_SCOPES='manage_orders:epoch-staging manage_customers:epoch-staging manage_products:epoch-staging manage_discount_codes:epoch-staging manage_customer_groups:epoch-staging manage_states:epoch-staging manage_categories:epoch-staging manage_types:epoch-staging'
elif [ "$1" = 'mt-epoch-staging2' ]; then
    echo 'Selected source mt-epoch-staging2'
    export SOURCE_CLIENT_ID='Vxjd_1L0y8LaupK2qWejWPOG'
    export SOURCE_CLIENT_SECRET='ODvZFpP6Kx7QJv0-rtVomCScFrTaaNIw'
    export SOURCE_PROJECT_KEY='epoch-staging2'
    export SOURCE_SCOPES='manage_states:epoch-staging2 manage_products:epoch-staging2 manage_categories:epoch-staging2 manage_customer_groups:epoch-staging2 manage_customers:epoch-staging2 manage_types:epoch-staging2 manage_discount_codes:epoch-staging2 manage_cart_discounts:epoch-staging2 manage_orders:epoch-staging2'
elif [ "$1" = 'mt-epoch-production8' ]; then
    echo 'Selected target mt-epoch-production8'
    export SOURCE_CLIENT_ID='a5HTd6TNhcepF4bHQSgoDUnQ'
    export SOURCE_CLIENT_SECRET='EKTls4tmpw9ZD0hcHMxIYiBRFREz4OQu'
    export SOURCE_PROJECT_KEY='epoch-production8'
    export SOURCE_SCOPES='manage_orders:epoch-production8 manage_types:epoch-production8 manage_states:epoch-production8 manage_products:epoch-production8 manage_customers:epoch-production8 manage_customer_groups:epoch-production8 manage_categories:epoch-production8'
elif [ "$1" = 'mt-epoch-production7' ]; then
    echo 'Selected target mt-epoch-production7'
    export SOURCE_CLIENT_ID='zz2WFQKWXybylhyZxx7jtUNw'
    export SOURCE_CLIENT_SECRET='SPMN3wRwLqGVjCdjBJmkO9eginikjn01'
    export SOURCE_PROJECT_KEY='epoch-production7'
    export SOURCE_SCOPES='manage_states:epoch-production7 manage_categories:epoch-production7 manage_orders:epoch-production7 manage_types:epoch-production7 manage_products:epoch-production7 manage_customer_groups:epoch-production7 manage_customers:epoch-production7'  
else
    echo "Script doesn't have info for CT project ${1}"
    exit 127
fi

if [ "$2" = 'epoch-staging2' ]; then
    echo 'Selected target epoch-staging2'
    export TARGET_CLIENT_ID='5WeaCOykrqu9s3PFVh5msY2k'
    export TARGET_CLIENT_SECRET='6lXSiFndzSxuHe04s8mHBSe3rZZXRqBc'
    export TARGET_PROJECT_KEY='epoch-staging2'
    export TARGET_SCOPES='manage_project:epoch-staging2 manage_api_clients:epoch-staging2 view_api_clients:epoch-staging2'
elif [ "$2" = 'epoch-staging' ]; then
    echo 'Selected target epoch-staging'
    export TARGET_CLIENT_ID='QvfgGcrqEQbJ_MGzyaJh0Sb_'
    export TARGET_CLIENT_SECRET='clKJexPspU7UEZtuNXscWQONamI5krCP'
    export TARGET_PROJECT_KEY='epoch-staging'
    export TARGET_SCOPES='manage_project:epoch-staging manage_api_clients:epoch-staging view_api_clients:epoch-staging'
elif [ "$2" = 'epoch-dev' ]; then
    echo 'Selected target epoch-dev'
    export TARGET_CLIENT_ID='VjADKLsGAKoUAXXQTD2L4uvB'
    export TARGET_CLIENT_SECRET='wpUuMPEW46NR_7E0CFPyTmzZqNCInQMz'
    export TARGET_PROJECT_KEY='epoch-dev'
    export TARGET_SCOPES='manage_project:epoch-dev manage_api_clients:epoch-dev view_api_clients:epoch-dev'
elif [ "$2" = 'epoch-production8' ]; then
    echo 'Selected target epoch-production8'
    export TARGET_CLIENT_ID='UazPpk_l5qq9fPgiNNleEZVJ'
    export TARGET_CLIENT_SECRET='dxso4nuPu4_oDgqvX9rOOSL4ApTSnN-N'
    export TARGET_PROJECT_KEY='epoch-production8'
    export TARGET_SCOPES='manage_project:epoch-production8 manage_api_clients:epoch-production8 view_api_clients:epoch-production8'
elif [ "$2" = 'epoch-production7' ]; then
    echo 'Selected target epoch-production7'
    export TARGET_CLIENT_ID='7GJbC6SLJM1btsLHo9cPMsoc'
    export TARGET_CLIENT_SECRET='ADIjH-l6BkB0pmtCJaCrBO6jf0YKD6ZW'
    export TARGET_PROJECT_KEY='epoch-production7'
    export TARGET_SCOPES='manage_project:epoch-production7 manage_api_clients:epoch-production7 view_api_clients:epoch-production7'
elif [ "$2" = 'epoch-production10' ]; then
    echo 'Selected target epoch-production10'
    export TARGET_CLIENT_ID='F8aGRiPuAUGsZN8m7QjFu0Fp'
    export TARGET_CLIENT_SECRET='e5UC_9ZaDwFjhCJ3FkwyPHycCMDJXYWK'
    export TARGET_PROJECT_KEY='epoch-production10'
    export TARGET_SCOPES='manage_project:epoch-production10 manage_api_clients:epoch-production10 view_api_clients:epoch-production10'
elif [ "$2" = 'epoch-production9' ]; then
    echo 'Selected target epoch-production9'
    export TARGET_CLIENT_ID='9rB6B3bJt5VqdgDZGty0xj3M'
    export TARGET_CLIENT_SECRET='RbZ1jsh5OCE6k0M8GYMFaJRLywqYx3-G'
    export TARGET_PROJECT_KEY='epoch-production9'
    export TARGET_SCOPES='manage_project:epoch-production9 manage_api_clients:epoch-production9 view_api_clients:epoch-production9'
elif [ "$2" = 'epoch-dataload' ]; then
    echo 'Selected target epoch-dataload'
    export TARGET_CLIENT_ID='6zvGDmcxx_kt6QkRPXzX6axF'
    export TARGET_CLIENT_SECRET='bqtdmFHPrRNvoP0AH_LiultyEAxNNYKc'
    export TARGET_PROJECT_KEY='epoch-dataload'
    export TARGET_SCOPES='manage_project:epoch-dataload manage_api_clients:epoch-dataload view_api_clients:epoch-dataload'
elif [ "$2" = 'mt-epoch-dev' ]; then
    echo 'Selected target mt-epoch-dev'
    export TARGET_CLIENT_ID='iTk5kNMC-Uuz2LripDSrHn3V'
    export TARGET_CLIENT_SECRET='pbYF6VBsbvHbvS1IzicNxJLS2YFw6ePi'
    export TARGET_PROJECT_KEY='epoch-dev'
    export TARGET_SCOPES='manage_project:epoch-dev manage_api_clients:epoch-dev view_api_clients:epoch-dev'
 elif [ "$2" = 'mt-epoch-staging' ]; then
    echo 'Selected target mt-epoch-staging'
    export TARGET_CLIENT_ID='lom_T-CZu3JA2N3osKYl4g74'
    export TARGET_CLIENT_SECRET='QAn64M-63n4rlpFyt2YQZmNq4TYoyfn3'
    export TARGET_PROJECT_KEY='epoch-staging'
    export TARGET_SCOPES='manage_orders:epoch-staging manage_customers:epoch-staging manage_products:epoch-staging manage_discount_codes:epoch-staging manage_customer_groups:epoch-staging manage_states:epoch-staging manage_categories:epoch-staging manage_types:epoch-staging'
elif [ "$2" = 'mt-epoch-staging2' ]; then
    echo 'Selected target mt-epoch-staging2'
    export TARGET_CLIENT_ID='Vxjd_1L0y8LaupK2qWejWPOG'
    export TARGET_CLIENT_SECRET='ODvZFpP6Kx7QJv0-rtVomCScFrTaaNIw'
    export TARGET_PROJECT_KEY='epoch-staging2'
    export TARGET_SCOPES='manage_states:epoch-staging2 manage_products:epoch-staging2 manage_categories:epoch-staging2 manage_customer_groups:epoch-staging2 manage_customers:epoch-staging2 manage_types:epoch-staging2 manage_discount_codes:epoch-staging2 manage_cart_discounts:epoch-staging2 manage_orders:epoch-staging2'
elif [ "$2" = 'mt-epoch-staging3' ]; then
    echo 'Selected target mt-epoch-staging3'
    export TARGET_CLIENT_ID='FncPxPo3aL9pl0gQJz52Urw7'
    export TARGET_CLIENT_SECRET='mNV35FXPDAO0uVgVsCZTO8V8D9H2tXI0'
    export TARGET_PROJECT_KEY='epoch-staging3'
    export TARGET_SCOPES='manage_cart_discounts:epoch-staging3 manage_customers:epoch-staging3 manage_customer_groups:epoch-staging3 manage_discount_codes:epoch-staging3 manage_orders:epoch-staging3 manage_types:epoch-staging3 manage_categories:epoch-staging3 manage_products:epoch-staging3 manage_states:epoch-staging3'
 elif [ "$2" = 'mt-epoch-production8' ]; then
    echo 'Selected target mt-epoch-production8'
    export TARGET_CLIENT_ID='a5HTd6TNhcepF4bHQSgoDUnQ'
    export TARGET_CLIENT_SECRET='EKTls4tmpw9ZD0hcHMxIYiBRFREz4OQu'
    export TARGET_PROJECT_KEY='epoch-production8'
    export TARGET_SCOPES='manage_orders:epoch-production8 manage_types:epoch-production8 manage_states:epoch-production8 manage_products:epoch-production8 manage_customers:epoch-production8 manage_customer_groups:epoch-production8 manage_categories:epoch-production8'
elif [ "$2" = 'mt-epoch-production7' ]; then
    echo 'Selected target mt-epoch-production7'
    export TARGET_CLIENT_ID='zz2WFQKWXybylhyZxx7jtUNw'
    export TARGET_CLIENT_SECRET='SPMN3wRwLqGVjCdjBJmkO9eginikjn01'
    export TARGET_PROJECT_KEY='epoch-production7'
    export TARGET_SCOPES='manage_states:epoch-production7 manage_categories:epoch-production7 manage_orders:epoch-production7 manage_types:epoch-production7 manage_products:epoch-production7 manage_customer_groups:epoch-production7 manage_customers:epoch-production7'
elif [ "$2" = 'mt-epoch-production10' ]; then
    echo 'Selected target mt-epoch-production10'
    export TARGET_CLIENT_ID='wa9HaKTdePYI-fS_H6NrFFiA'
    export TARGET_CLIENT_SECRET='hX2benn65ZGh4NdgFx3fdxaWL29PS1jJ'
    export TARGET_PROJECT_KEY='epoch-production10'
    export TARGET_SCOPES='manage_project:epoch-production10 manage_api_clients:epoch-production10 view_api_clients:epoch-production10'      

else
    echo "Script doesn't have info for CT project ${2}"
    exit 127
fi

if [[ "$1" == mt* ]]; then
    echo 'Selected source multi-tenant CT'    
    export SOURCE_HOST='https://api.us-east-2.aws.commercetools.com'
    export SOURCE_OAUTH_HOST='https://auth.us-east-2.aws.commercetools.com'
else
    echo 'Selected source old CT'    
    export SOURCE_HOST='https://api.att.us-east-1.aws.commercetools.com'
    export SOURCE_OAUTH_HOST='https://auth.att.us-east-1.aws.commercetools.com'
fi

if [[ "$2" == mt* ]]; then
    echo 'Selected target multi-tenant CT'    
    export TARGET_HOST='https://api.us-east-2.aws.commercetools.com'
    export TARGET_OAUTH_HOST='https://auth.us-east-2.aws.commercetools.com'
else
    echo 'Selected target old CT'    
    export TARGET_HOST='https://api.att.us-east-1.aws.commercetools.com'
    export TARGET_OAUTH_HOST='https://auth.att.us-east-1.aws.commercetools.com'
fi

SCRIPT_DIR="$(cd "$(dirname "${0}")" && pwd)"
echo "Running script on ${SCRIPT_DIR}"
cd $SCRIPT_DIR
