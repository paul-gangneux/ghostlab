CLEAN=false;
SERVER=false;
CLIENT=false;

while true; do
  case "$1" in
    -C | --clean ) CLEAN=true; shift ;;
    -s | --server ) SERVER=true; shift ;;
    -c | --client ) CLIENT=true; shift ;;
    ?* ) echo $1 "not a valid option"; shift ;;
    * ) break;;
  esac
done

if [ $SERVER == false ] && [ $CLIENT == false ] 
  then
    CLIENT=true;
    SERVER=true;
fi

if [ $SERVER == true ] 
  then
    cd server;
    if [ $CLEAN == true ] 
      then
        make clean;
      else 
        make;
    fi
    cd ..;
fi

if [ $CLIENT == true ] 
  then
    cd client;
    if [ $CLEAN == true ] 
      then
        make clean;
      else 
        make;
    fi
    cd ..;
fi