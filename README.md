pylxc
=====

A simple scala wrapper on LXC commands

> Note: This currently wraps lxc-* command line.

Usage
=====
You can query current containers using
   
    import com.cloud9ers.lxc._
    
    LXC.allAsList()
    >> List('Container1', 'Container2', 'Container3')
    lxc.allAsMap()
    >> Map('running' -> Seq('Container1'),
        'stopped': Seq('Container2', 'Container3')
       )
