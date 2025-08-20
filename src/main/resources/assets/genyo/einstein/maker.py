# i made the questions using chatgpt with this prompt:
# https://hasteb.in/m3HZvxQplMdDFiU

import yaml # type: ignore
import os

path = os.path.dirname(os.path.abspath(__file__)) + "/"
questions = dict()

with open(path + "questions.txt", "r", encoding="UTF-8") as file:
    index = 0
    
    for line in file:
        current = line.strip().split("|")
        
        if len(current) != 7:
            print("Line is incorrect.")
            continue
        
        #question, answers, correct, difficulty
        
        question = current[0]
        answers = [str(current[1]), str(current[2]), str(current[3]), str(current[4])]
        correct = current[5]
        difficulty = current[6]
        
        if difficulty != "Hard" and difficulty != "Easy":
            print(f"Difficulty is incorrect. ({difficulty})")
            continue
        
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
            correct = correct,
            difficulty = difficulty
        )
        
        questions[index] = data
        
        index += 1
        
with open(path + "einstein.yml", "w") as outfile:
    yaml.dump(questions, outfile, default_flow_style=True)