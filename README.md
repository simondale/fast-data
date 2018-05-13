# Apache Cassandra: Building a Fast Data Platform

This repository contains my talk about Apache Cassandra and Fast Data.

## Docker Containers
This talk covers the steps necessary to build a fast data platform. I have provided some
sample containers that illustrate this. It is possible to run these on a single machine
providing it has sufficient memory and CPUs. I used a virutal machine running Ubuntu 16.04
with 4 CPUs and 8GB of RAM.

The supporting containers can be built using the command:

`./build`

This will download all necessary files and create local containers.

**Please note that none of these containers should be considered production worthy**

## Source Code
The services that ingest data, process the stream and then make the results avaialable
are also available. These are found beneath the services subfolder.

You can build these individually with the sbt command:

`sbt assembly`

Building the docker containers (detailed above) will also create containers for the services.

**The services are not considered production worthy either**

## Deploying the containers
There is another script that can be used to deploy everything. In order to access tweets 
through the Twitter API you need to update env.sh to contain your own Twitter credentials.

I also run the following command to prevent them accidentally getting added to git:

`git update-index --assume-unchanged env.sh`

Once this is done, just run

`./deploy`

When you have finished you can then run

`./cleanup`

This wil delete any provisioned containers but leave the images.

## Presentation
The presentation can be found in the presentation subfolder.
