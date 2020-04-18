mkdir ~/download
mkdir ~/cluster

wget -P ~/download --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" https://download.java.net/java/GA/jdk13.0.2/d4173c853231432d94f001e99d882ca7/8/GPL/openjdk-13.0.2_linux-x64_bin.tar.gz

wget -P ~/download http://www-us.apache.org/dist/hadoop/common/hadoop-2.8.5/hadoop-2.8.5.tar.gz

wget -P ~/download https://downloads.apache.org/spark/spark-2.4.5/spark-2.4.5-bin-hadoop2.7.tgz
