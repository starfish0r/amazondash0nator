# amazondash0nator

This is a java project that uses either jnetpcap or the linux tcpdump binary to sniff network traffic.
The purpose of this thing is to have an amazon dash button act as a trigger for anything. I only implemented a wake on lan action.

When the button of a dash is pressed, it will boot up and connect to the configured wifi to send it's request to amazon. When entering the wifi, an ARP broadcast (containing the dash's mac address) is sent.
That broadcast is what this application is looking for. When that packet is found, the action is triggered.
