# Prophet-Chatbot
Prophet Team!

Hope this chatbot finds you well. Here is a demo video for this project: https://drive.google.com/file/d/1IHSfjtHVhFI4dtEEuYoWpDZgP5P7AK_U/view?usp=sharing

This program uses a hugging face data set, OpenAI's text-embeddings-002 for vectorization, ChromaDB for persistence, and gpt-3.5-turbo as an LLM.


To run this code, please have a Docker instance of ChromaDB running on port 8000.

**Prerequisites**:

You'll need to create the collection, and then provide that collectionID to ChromaCRUDHelper.java. ChromaCRUDHelper will execute the logic to ingest the dataset of choice (https://huggingface.co/datasets/TaylorAI/pubmed_other), as well as persist that information to Chroma.

** To Run the Chatbot (Raj):**

Please run the main method in the ChatBot.java . 


If you have any questions, you can shoot me an email - swarit01@gmail.com or drop a text/phone call @ 510-589-5041

Preprocessing/Ingestion:

<img width="343" alt="Screen Shot 2024-03-11 at 3 54 41 PM" src="https://github.com/swarit01/Prophet-Chatbot/assets/15681349/5817badd-09ef-4d70-b3d1-6ace16bc400c">


Chatbot Application:

<img width="1218" alt="Screen Shot 2024-03-11 at 1 57 02 PM" src="https://github.com/swarit01/Prophet-Chatbot/assets/15681349/778a9de9-a7e2-4f91-be7f-51bd9869ab1b">
