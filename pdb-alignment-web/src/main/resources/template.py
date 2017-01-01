#Python 2.7
#A Simple Python template program reading Genome Nexus G2S APIs

import requests
from requests.auth import HTTPDigestAuth
import json

#Setup API, e.g. g2s.genomenexus.org:8081/swagger-ui.html#!/Genome/GenomeStructureResidueMappingQuery
#Can change to any valid URL in Genome Nexus G2S API

hostName="http://g2s.genomenexus.org:8081/g2s/"
apiName="GenomeStructureResidueMappingQuery?chromosomeNum=" 
chromsomeNum="X"  
nuclPosition="66937331"
nuclType="T"

# Set up API URL
url = hostName+apiName+chromsomeNum+"&positionNum="+nuclPosition+"&nucleotideType="+nuclType

# Request API
myResponse = requests.get(url)
#print (myResponse.status_code)

# For successful API call, response code will be 200 (OK)
if(myResponse.ok):

    # Loads (Load String) takes a Json file and converts into python data structure (dict or list, depending on JSON)
	# In this Example, jData are lists of Residues from Genome Mapping to Protein Structures 
    jData = json.loads(myResponse.content)

	#pring output
    print jData
	print "\n"
else:
  # If response code is not ok (200), print the resulting http error code with description
    myResponse.raise_for_status()

