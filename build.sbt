import com.typesafe.config._

val conf       = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()
val appName    = conf.getString("app.name").toLowerCase().replaceAll("\\W+", "-")
val appVersion = conf.getString("app.version")

EclipseKeys.skipParents in ThisBuild := false
EclipseKeys.preTasks := Seq(compile in Compile)
EclipseKeys.projectFlavor := EclipseProjectFlavor.Java
EclipseKeys.eclipseOutput := Some(".target")
EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE18)
//EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
//EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)
EclipseKeys.createSrc := EclipseCreateSrc.ManagedClasses + EclipseCreateSrc.ManagedResources + EclipseCreateSrc.Unmanaged + EclipseCreateSrc.Managed + EclipseCreateSrc.Source + EclipseCreateSrc.Resource

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

routesGenerator := InjectedRoutesGenerator

lazy val root = (project in file(".")).enablePlugins(PlayJava).settings(
    name    := appName,
    version := appVersion
)

scalaVersion := "2.11.7"

resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"

val _springVersion           = "4.2.5.RELEASE"
val _ddthCacheAdapterVersion = "0.4.0"
val _akkaVersion             = "2.4.4"

libraryDependencies ++= Seq(
    "com.typesafe.akka"         %% "akka-cluster"               % _akkaVersion,
    "com.typesafe.akka"         %% "akka-cluster-metrics"       % _akkaVersion,
    "com.typesafe.akka"         %% "akka-cluster-tools"         % _akkaVersion,

    // DB for metadata: MySQL or PostgreSQL
    // DB connection pool: HikariCP or DBCP2
    "com.zaxxer"                %  "HikariCP"                   % "2.4.5",
    // "org.apache.commons"        %  "commons-dbcp2"              % "2.1.1",
    "mysql"                     %  "mysql-connector-java"       % "5.1.38",
    // "org.postgresql"            %  "postgresql"                 % "9.4.1208",

    "org.springframework"       %  "spring-beans"               % _springVersion,
    "org.springframework"       %  "spring-expression"          % _springVersion,
    "org.springframework"       %  "spring-jdbc"                % _springVersion,
    
    "redis.clients"             %  "jedis"                      % "2.8.1",
    "com.github.ddth"           %  "ddth-cache-adapter-core"    % _ddthCacheAdapterVersion,
    "com.github.ddth"           %  "ddth-cache-adapter-redis"   % _ddthCacheAdapterVersion,

    "com.github.ddth"           %  "ddth-commons-core"          % "0.4.0",

    // queue system: Kafka
    "com.github.ddth"           %  "ddth-kafka"                 % "1.2.1",

    "com.github.ddth"           %  "djs-commons"                % "0.1.3"
)
