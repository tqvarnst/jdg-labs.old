#!/bin/bash 
DEMO="JDG Workshop Labs"
AUTHORS="Thomas Qvarnstrom, Red Hat <tqvarnst@redhat.com>"
ASCII_WIDTH=52
DEFAULT_INSTALL_DIR=jdg-workshop
INSTALL_DIR=${DEFAULT_INSTALL_DIR}
PROG_NAME=`basename $0`
WORKDIR=`pwd`

echo $WORKDIR

# wipe screen.
clear 
echo

printf "##  %-${ASCII_WIDTH}s  ##\n" | sed -e 's/ /#/g'
printf "##  %-${ASCII_WIDTH}s  ##\n"   
printf "##  %-${ASCII_WIDTH}s  ##\n" "Setup script for ${DEMO}"
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

while getopts ":d:" opt; do
  case $opt in
    d)
      echo "Will use ${OPTARG} as installation dir" >&2
      INSTALL_DIR=$OPTARG
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 1
      ;;
    :)
      echo "Option -$OPTARG requires an argument." >&2
      exit 1
      ;;
  esac
done

if [[ "${INSTALL_DIR}" == "${DEFAULT_INSTALL_DIR}" ]]; then
	echo "Will use default ${INSTALL_DIR} as installation dir"
fi

if [ -x "${INSTALL_DIR}" ]; then
		echo "  - installtion directory ${INSTALL_DIR} already exists. Aborting!!"
		exit 99
fi

git --version >/dev/null 2>&1 || { echo >&2 "Git is required but not installed yet... aborting."; exit 1; }

mkdir "${INSTALL_DIR}" >/dev/null 2>&1 || { echo >&2 "Failed to create dir, Aborting!!!"; exit 99; }



# Get the name of the current branch
#git rev-parse --abbrev-ref HEAD

# Generate temp directories for GIT project and to store zip files
GIT_PROJECT=`mktemp -d XXXXXXXX`
LAB_ZIP_DIR=`mktemp -d XXXXXXXX`

# Get the full path to the temp dirs
GIT_PROJECT=${WORKDIR}/${GIT_PROJECT}
LAB_ZIP_DIR=${WORKDIR}/${LAB_ZIP_DIR}

echo "Cloning labs from github"
git clone https://github.com/tqvarnst/jdg-labs.git ${GIT_PROJECT} > /dev/null 2>&1 || { echo >&2 "Failed to clone project from github, aborting"; exit 98; }

cp -R ${GIT_PROJECT}/* ${INSTALL_DIR}

pushd ${GIT_PROJECT} > /dev/null

LABS=(lab1 lab2)

for lab in ${LABS[*]}
do
	echo "Downloading and extracting ${lab} into project dir"
	git branch "${lab}-start" "origin/${lab}-start" >/dev/null 2>&1 || { echo >&2 "Failed to branch ${lab}-start locally, aborting"; exit 97; }
	git checkout "${lab}-start"  >/dev/null 2>&1 || { echo >&2 "Failed to switch to branch ${lab}-start locally, aborting"; exit 96; }
	pushd projects/lab > /dev/null
	git archive -o "${LAB_ZIP_DIR}/${lab}.zip" --prefix="${lab}/" HEAD
	unzip -qq "${LAB_ZIP_DIR}/${lab}.zip" -d "${WORKDIR}/${INSTALL_DIR}/projects" >/dev/null
	wget -O "${WORKDIR}/${INSTALL_DIR}/projects/${lab}/${lab}-guide.pdf" https://gitprint.com/tqvarnst/jdg-labs/blob/${lab}-start/projects/lab/lab-guides/${lab}-guide.md?download
	popd >/dev/null
	#echo "Currently in git branch $(git rev-parse --abbrev-ref HEAD)"
done

for lab in ${LABS[*]}
do
	echo "Downloading ${lab}-solution.zip to project dir"
	git branch "${lab}-solution" "origin/${lab}-solution" >/dev/null 2>&1 || { echo >&2 "Failed to branch ${lab}-solution locally, aborting"; exit 97; }
	git checkout "${lab}-solution"  >/dev/null 2>&1 || { echo >&2 "Failed to switch to branch ${lab}-solution locally, aborting"; exit 96; }
	pushd projects/lab > /dev/null
	git archive -o "${LAB_ZIP_DIR}/${lab}-solution.zip" --prefix="${lab}-solution/" HEAD
	cp "${LAB_ZIP_DIR}/${lab}-solution.zip" "${WORKDIR}/${INSTALL_DIR}/projects/" >/dev/null
	popd >/dev/null
	#echo "Currently in git branch $(git rev-parse --abbrev-ref HEAD)"
done

git checkout master >/dev/null 2>&1
popd > /dev/null

rm -rf ${GIT_PROJECT}
rm -rf ${LAB_ZIP_DIR}

mkdir ${INSTALL_DIR}/installs
	
	

#GIT_CURRENT_BRANCH=`git rev-parse --abbrev-ref HEAD`

