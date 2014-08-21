#!/bin/bash 
DEMO="JDG Workshop Labs - Server environment"
JBOSS_HOME=./target/jboss-eap-6.3
JDG_HOME=./target/jboss-datagrid-6.3.0-server
SERVER_DIR=$JBOSS_HOME/standalone/deployments/
SERVER_CONF=$JBOSS_HOME/standalone/configuration/
SRC_DIR=./installs
EAP=jboss-eap-6.3.0.zip
JDG_SERVER=jboss-datagrid-6.3.0-server.zip
JDG_LIBRARY_MODUELS=jboss-datagrid-6.3.0-eap-modules-library.zip
HOTROD_MODULES=jboss-datagrid-6.3.0-eap-modules-hotrod-java-client.zip

# wipe screen.
clear 

echo

ASCII_WIDTH=50

printf "##  %-${ASCII_WIDTH}s  ##\n" | sed -e 's/ /#/g'
printf "##  %-${ASCII_WIDTH}s  ##\n"   
printf "##  %-${ASCII_WIDTH}s  ##\n" "Setting up the ${DEMO}"
printf "##  %-${ASCII_WIDTH}s  ##\n"
printf "##  %-${ASCII_WIDTH}s  ##\n"
printf "##  %-${ASCII_WIDTH}s  ##\n" "    # ####   ###   ###  ###   ###   ###"
printf "##  %-${ASCII_WIDTH}s  ##\n" "    # #   # #   # #    #      #  # #"
printf "##  %-${ASCII_WIDTH}s  ##\n" "    # ####  #   #  ##   ##    #  # #  ##"
printf "##  %-${ASCII_WIDTH}s  ##\n" "#   # #   # #   #    #    #   #  # #   #"
printf "##  %-${ASCII_WIDTH}s  ##\n" " ###  ####   ###  ###  ###    ###   ###"  
printf "##  %-${ASCII_WIDTH}s  ##\n"
printf "##  %-${ASCII_WIDTH}s  ##\n"
printf "##  %-${ASCII_WIDTH}s  ##\n"   
printf "##  %-${ASCII_WIDTH}s  ##\n" "brought to you by,"
printf "##  %-${ASCII_WIDTH}s  ##\n" "${AUTHORS}"
printf "##  %-${ASCII_WIDTH}s  ##\n"
printf "##  %-${ASCII_WIDTH}s  ##\n"
printf "##  %-${ASCII_WIDTH}s  ##\n" | sed -e 's/ /#/g'


echo
echo "Setting up the ${DEMO} environment..."
echo

# make some checks first before proceeding.	
DOWNLOADS=($EAP $JDG_SERVER $JDG_SERVER $JDG_LIBRARY_MODUELS $HOTROD_MODULES)

for DONWLOAD in ${DOWNLOADS[@]}
do
	if [[ -r $SRC_DIR/$DONWLOAD || -L $SRC_DIR/$DONWLOAD ]]; then
			echo $DONWLOAD are present...
			echo
	else
			echo You need to download $DONWLOAD from the Customer Support Portal 
			echo and place it in the $SRC_DIR directory to proceed...
			echo
			exit
	fi
done

# Create the target directory if it does not already exist.
if [ ! -x target ]; then
		echo "  - creating the target directory..."
		echo
		mkdir target
else
		echo "  - detected target directory, moving on..."
		echo
fi

# Move the old JBoss instance, if it exists, to the OLD position.
if [ -x $JBOSS_HOME ]; then
		echo "  - existing JBoss Enterprise EAP 6 detected..."
		echo
		echo "  - moving existing JBoss Enterprise EAP 6 aside..."
		echo
		rm -rf $JBOSS_HOME.OLD
		mv $JBOSS_HOME $JBOSS_HOME.OLD
fi
echo Unpacking new JBoss Enterprise EAP 6...
echo
unzip -q -d target $SRC_DIR/$EAP

# Creating and admin user with admin-123 as password
echo "Adding admin user"
$JBOSS_HOME/bin/add-user.sh -g admin -u admin -p admin-123 -s


# Adding JBoss Data Grid Library modules to EAP
echo "Adding JBoss Data Grid Modules to EAP"
tmpdir=`mktemp -d XXXXXXXX`
unzip -q -d ${tmpdir} ${SRC_DIR}/${JDG_LIBRARY_MODUELS}
cp -R ${tmpdir}/jboss-datagrid-6.3.0-eap-modules-library/modules/* $JBOSS_HOME/modules/
rm -rf  ${tmpdir} 

# Adding Hotrod modules to EAP
echo "Adding Hotrod Modules to EAP"
tmpdir=`mktemp -d XXXXXXXX`
unzip -q -d ${tmpdir} ${SRC_DIR}/${HOTROD_MODULES}
cp -R ${tmpdir}/jboss-datagrid-6.3.0-eap-modules-hotrod-java-client/modules/* $JBOSS_HOME/modules/
rm -rf  ${tmpdir} 

# Move the old JBoss instance, if it exists, to the OLD position.
if [ -x $JDG_HOME ]; then
		echo "  - existing JBoss Data Grid detected..."
		echo
		echo "  - moving existing JBoss Data Grid aside..."
		echo
		rm -rf $JDG_HOME.OLD
		mv $JDG_HOME $JDG_HOME.OLD
fi
# Unzip the JBoss DG instance.
echo Unpacking new JBoss Data Grid instance...
echo
unzip -q -d target $SRC_DIR/$JDG_SERVER

echo "Done setting up environment"
