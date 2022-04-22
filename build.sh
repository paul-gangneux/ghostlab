CLEAN=false;
SERVER=false;
CLIENT=false;
DEBUG=false;

while true; do
  case "$1" in
    -c | -clean ) CLEAN=true; shift ;;
    -s | -server ) SERVER=true; shift ;;
    -cli | -client ) CLIENT=true; shift ;;
    -d | -debug ) DEBUG=true; shift ;;
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
        :
      else 
        :
    fi
    cd ..;
fi