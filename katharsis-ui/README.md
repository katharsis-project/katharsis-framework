Goals of this project
=====================
Per other discussions, I created a package for a default UI. It's nothing more than that though: a package.

I wanted to create this issue so we can talk about how to proceed.

Key features (I think):

1. May be picked up a maven dependency and served up as `/index.html` so that you can immediately begin interacting with it
2. It would be nice is it was friendly to other languages and tools -- that way the UI can be picked up even in a non-java environment (would this mean multi module? Multi build (mvn, npm))

Pull request [#125](https://github.com/katharsis-project/katharsis-framework/pull/125)  
Issue tracking [#165](https://github.com/katharsis-project/katharsis-framework/issues/165)  

To begin developing, there is already a package.json file that is react ready. Currently, it's totally broke 'cause I moved it to the resources directory to be java friendly
This shouldn't be a problem to overcome for the savvy developer. 

Next, you can/should [install docker](https://docs.docker.com/engine/installation/) so that you don't have to worry about the data service or port mappings or CORS.

There is a haproxy.cfg file locally that should do the trick

* you need to add this loopback alias for mac
`sudo ifconfig lo0 alias 10.200.10.1/24`
* everytime you restart docker it's going to pickup changes in the file. Use a fully qualified path to remove problems
`docker run -d -p 80:80   --name katharsis -v /path/to/haproxy.cfg:/usr/local/etc/haproxy/haproxy.cfg:ro haproxy`

What this docker command says is: 
1. start a container called 'haproxy' as a daemon (`-d`) 
2. binding your development path to the haproxy file to the containers internal path (`-v`)
3. Listen on real-machine port 80 and direct that to container port 80 (where haproxy will be listening) (`-p 80:80`)
4. And name the container 'katharsis' (`--name`). You will use this name in the future to restart it if necessary with `docker restart katharsis`


then hit [localhost](http://localhost)


Expected problems developing this project
=========================================
1. Resource discoverability (the UI needs to start at the root and work it's way down). Currently disscoverability isn't supported in katharsis
2. Functional discoverability. The UI will need to know what HTTP verbs are possible on which resources. This would be done with an `OPTIONS` which also is not supported.
