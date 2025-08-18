import yaml # type: ignore
import os

path = os.path.dirname(os.path.abspath(__file__)) + "/"
questions = dict()

with open(path + "questions.txt", "r", encoding="UTF-8") as file:
    index = 0
    
    for line in file:
        current = line.strip().split("|")
                
        #question, answers, correct
        
        question = current[0]
        answers = [current[1], current[2], current[3], current[4]]
        correct = current[5]
        
        if correct == None:
            break
        
        data = dict(
            index = str(index),
            question = question,
            answers = dict(
                A = answers[0],
                B = answers[1],
                C = answers[2],
                D = answers[3]
            ),
            correct = correct
        )
        
        questions[index] = data
        
        index += 1
        
with open(path + "einstein.yml", "w") as outfile:
    yaml.dump(questions, outfile, default_flow_style=True)