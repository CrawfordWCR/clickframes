#!/bin/bash
#!/bin/bash
set -x
rsync="rsync --verbose --progress --delete-after -azC"
ssh="ssh -l root clickframes.org"
$rsync /root/.m2/repository/org/clickframes/clickframes-clipr/0.9.1-SNAPSHOT/clickframes-clipr-0.9.1-SNAPSHOT.war root@clickframes.org:/tmp/clipr.war
$ssh chown jboss:jboss /tmp/clipr.war
$ssh mv /tmp/clipr.war /opt/jboss4/server/default/deploy/