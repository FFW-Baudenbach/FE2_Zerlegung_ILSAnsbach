# FE2_Zerlegung_ILSAnsbach
## Motivation
Dieses Repository beinhaltet eine eigene Zerlegung für [Alamos FE2](https://alamos-support.atlassian.net/wiki/spaces/documentation/pages/219480670/Eigene+Zerlegung+entwickeln). 
Sie wird genutzt bei der [Freiwilligen Feuerwehr Markt Baudenbach](https://www.ffw-baudenbach.de), welche im Alarmfall ein Alarmfax der ILS Ansbach bekommt.  
Dieses auszuwerten funktioniert auch mit Bordmitteln von FE2 (Textzerlegung, Globale Ersetzungen, Alarmablauf). Es ist jedoch schwierig, die Einsatzmittel aus dem Text damit zu extrahieren, so dass FE2 mit diesen arbeiten kann, bspw. alarmierte Fahrzeuge.  
## Funktionalitäten
Der Code orientiert sich am Ablauf an dem regulären FE2-Ablauf 
* Globale Ersetzung mittels Regex (behebt typische Texterkennungsfehler)
* Textzerlegung (die groben Blöcke aus dem Alarmfax)
* Adresserkennung (extrahiert die Parameter street, house, city, etc.)
* Fahrzeugerkennung  
Da dies kein Feature von FE2 im Alarmablauf ist, wird hier der Parameter _vehicles_ gesetzt. Diese Variable muss __exakt__ die Fahrzeugnamen oder -codes in einer eigenen Zeile beinhalten. Das ist schwierig aus dem Fax ohne eigene Logik.  
Zusätzlich wird auch eine Variable geschrieben, die alle Einsatzmittel, also auch andere alarmierte Feuerwehren enthält und so aufbereitet ist, dass sie direkt im Alarmablauf verwendet werden kann. In einer anderen Variable wird das ganze noch im HTML-Format ausgegeben um es aufbereitet per Mail rausschicken zu können.
* Für die Stichworterkennung und Addresserkennung aus Koordinaten wird weiterhin die FE2-Funktionalität verwendet.
* Im Falle einer Exception wird diese nicht an FE2 durchgereicht (keine Ahnung was da passieren würde). Stattdessen wird diese in einen Parameter geschrieben. Ein funktionierender Alarm ist dann eh nicht mehr möglich, aber es lässt sich damit die Ursache leichter erkennen.
* Stellt sicher, dass mit der korrekten Java Version kompiliert wird
* Beinhaltet anonymisiertes Alarmfax als UnitTest
## Verwendung
* Lade ![FE2_Zerlegung_ILSAnsbach.jar](/FE2_Zerlegung_ILSAnsbach.jar?raw=true "FE2_Zerlegung_ILSAnsbach.jar") herunter
* Kopiere Datei nach _...\FE2\Config\data\extern_
* In _FE2 Administration_ kann nun die Zerlegung an der Einheit unter _Einstellungen > Zerlegung_ ausgewählt werden
* Prüfe, ob die Version mit der letzten Build-Version übereinstimmt (siehe [info.properties](src/main/resources/info.properties))