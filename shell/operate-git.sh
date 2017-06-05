#!/bin/bash

#define release branch name
release_date=`date +%Y%m%d`
release_branch_name="release-"${release_date}

#cd to jzsec local
cd /home/gituser/ly/jzsec

#checkout master and update
git checkout master
git pull origin master:master

#deal spd
space_row=`sed -n -e '/^[[:space:]]*$/=' /home/gituser/ly/jzsec/config/kcbpspd.xml`
if [ ! -n "$space_row" ]; then
echo "no space row, next step!"
else
row_num=`sed -n '$=' /home/gituser/ly/jzsec/config/kcbpspd.xml`
echo -e "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n<kcbpspd>\n" > /home/gituser/ly/jzsec/config/kcbpspd-release-${release_date}.xml
sed -n ''$(($space_row+1))','$(($row_num-1))'p' /home/gituser/ly/jzsec/config/kcbpspd.xml >> /home/gituser/ly/jzsec/config/kcbpspd-release-${release_date}.xml
echo -e "\n</kcbpspd>" >> /home/gituser/ly/jzsec/config/kcbpspd-release-${release_date}.xml
sed -i ''${space_row}'d' /home/gituser/ly/jzsec/config/kcbpspd.xml
git add .
git commit -am "new incremental spd and merge parent spd"
fi
if [ ! -s /home/gituser/ly/jzsec/sql_upgrade/jzjy_upgrade.sql ]; then
git rm /home/gituser/ly/jzsec/sql_upgrade/jzjy_upgrade.sql
fi
if [ ! -s /home/gituser/ly/jzsec/sql_upgrade/rzrq_upgrade.sql ]; then
git rm /home/gituser/ly/jzsec/sql_upgrade/rzrq_upgrade.sql
fi
git commit -am "delete null sql"
git push origin master

#deal release branch
#git push origin :${release_branch_name}
#git branch -D ${release_branch_name}
git branch ${release_branch_name}
#git push origin ${release_branch_name}
git checkout ${release_branch_name}
sed -i "/ProductVersion/{s/.*/            VALUE \"ProductVersion\", \"${release_branch_name}\"/}" /home/gituser/ly/jzsec/lbm_jzsec/lbm_jzsec/lbm_jzsec.rc
sed -i "/exec/{s/.*/exec nb_jzsecupgrade_version '${release_branch_name}',''/}" /home/gituser/ly/jzsec/sql_upgrade/ver.sql
if [ -f /home/gituser/ly/jzsec/sql_upgrade/jzjy_upgrade.sql ]; then
echo -e "\n" >> /home/gituser/ly/jzsec/sql_upgrade/upgrade.bat
echo "kd30sql -T -U___ -P___ -S___ -ESQL .\jzjy_upgrade.sql" >> /home/gituser/ly/jzsec/sql_upgrade/upgrade.bat
fi
if [ -f /home/gituser/ly/jzsec/sql_upgrade/rzrq_upgrade.sql ]; then
echo -e "\n" >> /home/gituser/ly/jzsec/sql_upgrade/upgrade.bat
echo "kd30sql -T -U___ -P___ -S___ -ESQL .\rzrq_upgrade.sql" >> /home/gituser/ly/jzsec/sql_upgrade/upgrade.bat
fi
if [ `sed -n '$=' /home/gituser/ly/jzsec/sql_upgrade/upgrade.bat` -ne 1 ]; then
#zip -r sql-${release_branch_name}.zip sql_upgrade/*
tar -cvf sql-${release_branch_name}.tar sql_upgrade/*
fi
git add .
git commit -am "re version"
git push origin ${release_branch_name}

#checkout to master,deal with spd and sql
git checkout master
git pull origin master:master
if [ -f /home/gituser/ly/jzsec/config/kcbpspd-release-${release_date}.xml ]; then
git rm /home/gituser/ly/jzsec/config/kcbpspd-release-${release_date}.xml
fi
if [ -f /home/gituser/ly/jzsec/sql_upgrade/jzjy_upgrade.sql ]; then
git rm /home/gituser/ly/jzsec/sql_upgrade/jzjy_upgrade.sql
fi
if [ -f /home/gituser/ly/jzsec/sql_upgrade/rzrq_upgrade.sql ]; then
git rm /home/gituser/ly/jzsec/sql_upgrade/rzrq_upgrade.sql
fi
git commit -am "delete incremental spd and sql"
touch /home/gituser/ly/jzsec/sql_upgrade/jzjy_upgrade.sql
touch /home/gituser/ly/jzsec/sql_upgrade/rzrq_upgrade.sql
git add .
git commit -am "renew sql"
git push origin master
