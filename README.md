Hallo,

Dit is work in progress van een applicatie die ik voor thuisgebruik aan het bouwen ben.

Het idee is dat een Java applicatie de status van mijn Bluesound streamer en wi-fi speakers uitleest en (uiteindelijk) naar een RabbitMQ instance door zal sturen, waar andere applicaties deze data ontvangen en er (ooit) mee aan de haal gaan (bijvoorbeeld scrobblen naar Last.FM, of een web-based dashboard real-time verversen...)

Op dit moment heb ik alleen code om (vrij low-level) spelers te detecteren. Bluesound heeft het wiel opnieuw uitgevonden en geen gebruik gemaakt van standaarden als SSDP en mDNS om hun apparatuur op een netwerk detecteerbaar te maken, maar ze hebben hun eigen "LSDP" protocol in het leven geroepen. Ik heb dit protocol geimplementeerd.

Op het moment van schrijven is de huidige versie van dit protocol beschreven in dit document:
https://nadelectronics.com/wp-content/uploads/2022/07/BluOS-Custom-Integration-API-v1.5.pdf

De volgende stappen zijn de spelers met een REST API te gaan pollen en events naar RabbitMQ te sturen.

Als ik dat voor elkaar heb kan ik iets met die data gaan doen.

Op dit moment heb ik alleen unit-testen op `bluos-handler\src\main\java\nl\vincentvanderleun\bluos\service\impl\lsdp\LsdpPacketInspector.java`, dit wil ik komende tijd uit gaan breiden.

Ik hoop dat dit een beetje een beeld geeft van waar ik mee bezig ben en wat ik (uiteindelijk) wil bereiken.

De code wil ik tzt nog netter maken door builders te maken, een wrapper om de UDP socket te maken, zodat het beter testbaar en mockbaar wordt, etc. etc. etc.

-- Vincent