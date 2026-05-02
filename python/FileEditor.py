import sys
from pymongo import MongoClient
from pymongo.errors import ConnectionFailure

def addPerson(name,phone=None,address=None,gender=None):
    try:
        client = MongoClient("mongodb+srv://jonahisaacgorski_db_user:GDqOTWGsBYOksyKc@hello.qep1csa.mongodb.net/", serverSelectionTimeoutMS=5000)
        client.admin.command("ping")
        print("Connected to MongoDB")
        db = client["test"]
        collection = db["users"]
        user = {"name": name,"phone":phone,"address":address,"gender":gender}
        result = collection.insert_one(user)
        print(f"Inserted document ID: {result.inserted_id}")
        for doc in collection.find():
            print(doc)
    except ConnectionFailure as e:
        print(f"Could not connect to MongoDB: {e}")
    finally:
        client.close()

addPerson(sys.argv[1],"hi")