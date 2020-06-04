import dash
import dash_core_components as dcc
import dash_html_components as html
import pandas as pd
import plotly.graph_objs as go
from collections import defaultdict
import plotly.express as px
import os


app = dash.Dash()

# df = pd.read_csv(
#     'https://gist.githubusercontent.com/chriddyp/' +
#     '5d1ea79569ed194d432e56108a04d188/raw/' +
#     'a9f9e8076b837d541398e999dcbac2b2826a81f8/'+
#     'gdp-life-exp-2007.csv')

# Fetch all benchmarks
df = pd.read_csv('results.out')

benchmarks = df['benchmark'].drop_duplicates()

results = defaultdict(dict)

for i, row in df.iterrows():
    bm = os.path.basename(row['benchmark'])
    solver = row['solver']
    time = row['time']
    results[bm][solver] = time


x = []
y = []

for k in results:
    x.append(results[k]['z3_'])
    y.append(results[k]['uppsat_SmallFloats'])


app.layout = html.Div([
    dcc.Graph(
        id='z3 vs SmallFloats',
        figure={
            'data': [
                go.Scatter(
                    x=x,
                    y=y,
                    text='test',
                    mode='markers',
                    opacity=0.8,
                    marker={
                        'size': 15,
                        'line': {'width': 0.5, 'color': 'white'}
                    },
                    name=i
                ) 
            ],
            'layout': go.Layout(
                xaxis={'title': 'SmallFloats'},
                yaxis={'title': 'z3'},
                margin={'l': 40, 'b': 40, 't': 10, 'r': 10},
                legend={'x': 0, 'y': 1},
                hovermode='closest'
            )
        }
    )
])

if __name__ == '__main__':
    app.run_server(debug=True)



